package com.expensesapp.server.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesapp.server.dto.ExpenseRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Expense;
import com.expensesapp.server.service.expense.ExpenseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(expenseService.createExpense(request, authUser));
    }

    @GetMapping
    public ResponseEntity<List<Expense>> viewMyExpenses(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(expenseService.getMyExpenses(authUser));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Expense> settleBill(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(expenseService.payUpcomingBill(id, authUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request, authUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthUser authUser) {
        expenseService.deleteExpense(id, authUser);
        return ResponseEntity.noContent().build();
    }
}
