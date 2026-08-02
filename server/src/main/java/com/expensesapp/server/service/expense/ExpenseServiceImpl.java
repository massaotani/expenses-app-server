package com.expensesapp.server.service.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesapp.server.dto.ExpenseRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Card;
import com.expensesapp.server.model.Expense;
import com.expensesapp.server.model.User;
import com.expensesapp.server.model.enums.CardType;
import com.expensesapp.server.model.enums.PaymentType;
import com.expensesapp.server.repository.CardRepository;
import com.expensesapp.server.repository.ExpenseRepository;
import com.expensesapp.server.repository.UserRepository; // FIXED: Added missing import

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository; // FIXED: Injected missing repository

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getMyExpenses(AuthUser authUser) {
        return expenseRepository.findByUser_IdOrderByDueDateDesc(authUser.getUserProfile().getId());
    }

    @Override
    @Transactional
    public Expense createExpense(ExpenseRequest request, AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserProfile().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        Card card = null;

        // 1. Conditional Card Validation
        if (request.getPaymentType() == PaymentType.CARD) {
            if (request.getCardId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Card selection is required for card payments.");
            }

            card = cardRepository.findById(request.getCardId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Selected card not found."));

            if (!card.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access to this card.");
            }
        }

        // 2. Build out the Expense Engine Configuration
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .value(request.getValue())
                .category(request.getCategory())
                .dueDate(request.getDueDate())
                .isPaid(request.isPaid())
                .recurrencePeriod(request.getRecurrencePeriod())
                .paymentType(request.getPaymentType())
                .card(card) // Will be null if payment type is CASH
                .user(user)
                .build();

        // 3. Process immediate balance adjustments if the bill is already paid
        if (request.isPaid()) {
            expense.setPaidAt(LocalDateTime.now());

            // Deduct from card balance if card was used
            if (expense.getPaymentType() == PaymentType.CARD && card != null) {
                processCardDeduction(card, request.getValue());
            }

            // Cash payments bypass card processing completely and just update the total running cost
            user.setMonthlyExpenses(user.getMonthlyExpenses().add(request.getValue()));
            userRepository.save(user);
        }

        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public Expense payUpcomingBill(UUID expenseId, AuthUser authUser) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense record not found"));

        if (!expense.getUser().getId().equals(authUser.getUserProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action.");
        }

        if (expense.isPaid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This expense has already been settled.");
        }

        expense.setPaid(true);
        expense.setPaidAt(LocalDateTime.now());

        User user = expense.getUser();

        // FIXED: Route card settlements to card processing logic, matching createExpense behavior
        if (expense.getPaymentType() == PaymentType.CARD && expense.getCard() != null) {
            processCardDeduction(expense.getCard(), expense.getValue());
        }
        
        // Cash transactions skip card alterations entirely and only increment the total spent so far
        user.setMonthlyExpenses(user.getMonthlyExpenses().add(expense.getValue()));
        userRepository.save(user);

        return expenseRepository.save(expense);
    }

    private void processCardDeduction(Card card, BigDecimal value) {
        if (card.getCardType() == CardType.CREDIT) {
            // Credit cards track liabilities going up
            card.setCurrentBalance(card.getCurrentBalance().add(value));
        } else if (card.getCardType() == CardType.DEBIT) {
            // Debit cards track active liquid funds going down
            if (card.getCurrentBalance().compareTo(value) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds on this debit card.");
            }
            card.setCurrentBalance(card.getCurrentBalance().subtract(value));
        }
        cardRepository.save(card);
    }
}