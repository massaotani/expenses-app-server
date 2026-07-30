package com.expensesapp.server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesapp.server.dto.PaymentMethodRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.PaymentMethod;
import com.expensesapp.server.service.payment.PaymentMethodService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<PaymentMethod> addPaymentMethod(
            @Valid @RequestBody PaymentMethodRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(paymentMethodService.createPaymentMethod(request, authUser));
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethod>> getMyBalances(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(paymentMethodService.getAllMethodsForUser(authUser));
    }
}
