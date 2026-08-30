package com.expensesapp.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.MonthlyBalance;
import com.expensesapp.server.service.balance.MonthlyBalanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/monthly-balances")
@RequiredArgsConstructor
public class MonthlyBalanceController {

    private final MonthlyBalanceService monthlyBalanceService;

    @GetMapping("/current")
    public ResponseEntity<MonthlyBalance> getCurrentMonthBalance(@AuthenticationPrincipal AuthUser authUser) {
        MonthlyBalance balance = monthlyBalanceService.getCurrentMonthBalance(authUser);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{year}/{month}")
    public ResponseEntity<MonthlyBalance> getMonthBalance(
            @PathVariable int year,
            @PathVariable int month,
            @AuthenticationPrincipal AuthUser authUser) {
        MonthlyBalance balance = monthlyBalanceService.getMonthBalance(authUser, year, month);
        return ResponseEntity.ok(balance);
    }
}