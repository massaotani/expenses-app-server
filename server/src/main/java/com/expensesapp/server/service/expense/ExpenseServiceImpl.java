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
import com.expensesapp.server.model.MonthlyBalance;
import com.expensesapp.server.model.User;
import com.expensesapp.server.model.enums.PaymentType;
import com.expensesapp.server.repository.CardRepository;
import com.expensesapp.server.repository.ExpenseRepository;
import com.expensesapp.server.repository.MonthlyBalanceRepository;
import com.expensesapp.server.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final MonthlyBalanceRepository monthlyBalanceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getMyExpenses(AuthUser authUser, Integer year, Integer month) {
        UUID userId = authUser.getUserProfile().getId();
        if (year != null && month != null) {
            return expenseRepository.findByUserIdAndYearAndMonth(userId, year, month);
        }
        return expenseRepository.findByUser_IdOrderByDueDateDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyBalance getMonthlyBalance(AuthUser authUser, Integer year, Integer month) {
        User user = userRepository.findById(authUser.getUserProfile().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        return monthlyBalanceRepository.findByUser_IdAndYearAndMonth(user.getId(), year, month)
                .orElseGet(() -> MonthlyBalance.builder()
                        .user(user)
                        .year(year)
                        .month(month)
                        .income(user.getMonthlyIncome())
                        .totalExpenses(BigDecimal.ZERO)
                        .savings(user.getMonthlyIncome())
                        .build());
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
        }

        Expense savedExpense = expenseRepository.save(expense);
        recalculateMonthlyBalance(user, request.getDueDate().getYear(), request.getDueDate().getMonthValue());

        return savedExpense;
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

        Expense savedExpense = expenseRepository.save(expense);
        recalculateMonthlyBalance(expense.getUser(), expense.getDueDate().getYear(), expense.getDueDate().getMonthValue());

        return savedExpense;
    }

    @Override
    @Transactional
    public Expense updateExpense(UUID id, ExpenseRequest request, AuthUser authUser) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense record not found"));

        if (!expense.getUser().getId().equals(authUser.getUserProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action.");
        }

        int oldYear = expense.getDueDate().getYear();
        int oldMonth = expense.getDueDate().getMonthValue();

        User user = expense.getUser();

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

        expense.setDescription(request.getDescription());
        expense.setValue(request.getValue());
        expense.setCategory(request.getCategory());
        expense.setDueDate(request.getDueDate());
        expense.setPaymentType(request.getPaymentType());
        expense.setRecurrencePeriod(request.getRecurrencePeriod());
        expense.setPaid(request.isPaid());
        expense.setCard(card);

        if (request.isPaid()) {
            if (expense.getPaidAt() == null) {
                expense.setPaidAt(LocalDateTime.now());
            }
        } else {
            expense.setPaidAt(null);
        }

        Expense updatedExpense = expenseRepository.save(expense);

        // Recalculate for both old and new dates in case the month/year changed
        recalculateMonthlyBalance(user, oldYear, oldMonth);
        recalculateMonthlyBalance(user, request.getDueDate().getYear(), request.getDueDate().getMonthValue());

        return updatedExpense;
    }

    @Override
    @Transactional
    public void deleteExpense(UUID id, AuthUser authUser) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense record not found"));

        if (!expense.getUser().getId().equals(authUser.getUserProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action.");
        }

        int year = expense.getDueDate().getYear();
        int month = expense.getDueDate().getMonthValue();
        User user = expense.getUser();

        expenseRepository.delete(expense);
        recalculateMonthlyBalance(user, year, month);
    }

    private void recalculateMonthlyBalance(User user, int year, int month) {
        List<Expense> monthExpenses = expenseRepository.findByUserIdAndYearAndMonth(user.getId(), year, month);

        BigDecimal totalSpent = monthExpenses.stream()
                .filter(Expense::isPaid)
                .map(Expense::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal income = user.getMonthlyIncome();
        BigDecimal savings = income.subtract(totalSpent);

        MonthlyBalance balance = monthlyBalanceRepository
                .findByUser_IdAndYearAndMonth(user.getId(), year, month)
                .orElseGet(() -> MonthlyBalance.builder()
                        .user(user)
                        .year(year)
                        .month(month)
                        .build());

        balance.setIncome(income);
        balance.setTotalExpenses(totalSpent);
        balance.setSavings(savings);

        monthlyBalanceRepository.save(balance);
    }
}