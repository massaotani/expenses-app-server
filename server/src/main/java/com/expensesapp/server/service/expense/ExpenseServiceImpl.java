package com.expensesapp.server.service.expense;

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
import com.expensesapp.server.model.enums.PaymentType;
import com.expensesapp.server.repository.CardRepository;
import com.expensesapp.server.repository.ExpenseRepository;
import com.expensesapp.server.repository.UserRepository;

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

        if (request.getPaymentType() == PaymentType.CARD) {
            if (request.getCardId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card selection is required for card payments.");
            }

            card = cardRepository.findById(request.getCardId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Selected card not found."));

            if (!card.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access to this card.");
            }
        }

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .value(request.getValue())
                .category(request.getCategory())
                .dueDate(request.getDueDate())
                .isPaid(request.isPaid())
                .recurrencePeriod(request.getRecurrencePeriod())
                .paymentType(request.getPaymentType())
                .card(card)
                .user(user)
                .build();

        if (request.isPaid()) {
            expense.setPaidAt(LocalDateTime.now());
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

        // Increment total spent since card balances are no longer tracked
        user.setMonthlyExpenses(user.getMonthlyExpenses().add(expense.getValue()));
        userRepository.save(user);

        return expenseRepository.save(expense);
    }
}
