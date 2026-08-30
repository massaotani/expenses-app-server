package com.expensesapp.server.service.expense;

import java.util.List;
import java.util.UUID;

import com.expensesapp.server.dto.ExpenseRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Expense;
import com.expensesapp.server.model.MonthlyBalance;

public interface ExpenseService {

    List<Expense> getMyExpenses(AuthUser authUser, Integer year, Integer month);

    Expense createExpense(ExpenseRequest request, AuthUser authUser);

    Expense payUpcomingBill(UUID expenseId, AuthUser authUser);

    Expense updateExpense(UUID id, ExpenseRequest request, AuthUser authUser);

    void deleteExpense(UUID id, AuthUser authUser);

    MonthlyBalance getMonthlyBalance(AuthUser authUser, Integer year, Integer month);
}
