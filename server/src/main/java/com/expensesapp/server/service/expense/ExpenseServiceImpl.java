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
import com.expensesapp.server.model.Expense;
import com.expensesapp.server.model.User;
import com.expensesapp.server.model.enums.PaymentType;
import com.expensesapp.server.repository.ExpenseRepository;
import com.expensesapp.server.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository expenseRepository;
    // private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getMyExpenses(AuthUser authUser) {
        return expenseRepository.findByUser_IdOrderByDueDateDesc(authUser.getUserProfile().getId());
    }

    @Override
    @Transactional
    public Expense createExpense(ExpenseRequest request, AuthUser authUser) {
        // 1. Fetch the managed User record to safely perform financial updates
        User user = userRepository.findById(authUser.getUserProfile().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        // 2. Build out the simplified Expense configuration
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .value(request.getValue())
                .category(request.getCategory())
                .dueDate(request.getDueDate())
                .isPaid(request.isPaid())
                .recurrencePeriod(request.getRecurrencePeriod())
                .paymentType(request.getPaymentType()) // Set the direct enum type
                .user(user)
                .build();

        // 3. Process immediate financial shifts if the expense is logged as paid
        if (request.isPaid()) {
            expense.setPaidAt(LocalDateTime.now());

            // Execute down-scaling on available income pools for cash/debit entries
            processDirectDeduction(user, request.getPaymentType(), request.getValue());

            // Accumulate onto the user's running monthly tracker
            user.setMonthlyExpenses(user.getMonthlyExpenses().add(request.getValue()));
            userRepository.save(user); // Commits the updated metrics to the DB
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

        // Apply deductions during manual bill settlement
        processDirectDeduction(user, expense.getPaymentType(), expense.getValue());

        user.setMonthlyExpenses(user.getMonthlyExpenses().add(expense.getValue()));
        userRepository.save(user);

        return expenseRepository.save(expense);
    }

    private void processDirectDeduction(User user, PaymentType type, BigDecimal value) {
        if (type == PaymentType.CASH || type == PaymentType.DEBIT) {
            BigDecimal newIncomeBalance = user.getMonthlyIncome().subtract(value);

            if (newIncomeBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Transaction declined: Insufficient funds in your Income Pot.");
            }
            user.setMonthlyIncome(newIncomeBalance);
        }
        // If type == CREDIT, we don't touch monthlyIncome, it just builds
        // user.monthlyExpenses
    }
}
