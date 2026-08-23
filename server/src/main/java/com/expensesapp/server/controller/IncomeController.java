package com.expensesapp.server.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesapp.server.dto.IncomeDepositRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Income;
import com.expensesapp.server.service.income.IncomeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<Income> createIncome(
            @Valid @RequestBody IncomeDepositRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        Income income = incomeService.createIncome(request, authUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(income);
    }

    @GetMapping
    public ResponseEntity<List<Income>> getUserIncomes(
            @AuthenticationPrincipal AuthUser authUser) {
        List<Income> incomes = incomeService.getUserIncomes(authUser);
        return ResponseEntity.ok(incomes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Income> updateIncome(
            @PathVariable UUID id,
            @Valid @RequestBody IncomeDepositRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        Income updatedIncome = incomeService.updateIncome(id, request, authUser);
        return ResponseEntity.ok(updatedIncome);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthUser authUser) {
        incomeService.deleteIncome(id, authUser);
        return ResponseEntity.noContent().build();
    }
}
