package com.expensesapp.server.service.expense;

import java.util.List;
import java.util.UUID;

import com.expensesapp.server.dto.ExpenseRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Expense;

public interface ExpenseService {
    List<Expense> getMyExpenses(AuthUser authUser);
    
    Expense createExpense(ExpenseRequest request, AuthUser authUser);

    Expense payUpcomingBill(UUID expenseId, AuthUser authUser);

    Expense updateExpense(UUID id, ExpenseRequest request, AuthUser authUser);

    void deleteExpense(UUID id, AuthUser authUser);
}
