package com.expensesapp.server.dto;

import java.math.BigDecimal;

import com.expensesapp.server.model.enums.PaymentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class PaymentMethodRequest {

    @NotBlank(message = "Payment method name is required")
    private String name;

    @NotNull(message = "Payment type (CASH, CREDIT, etc.) is required")
    private PaymentType type;

    @PositiveOrZero(message = "Credit limit cannot be negative")
    private BigDecimal creditLimit;

    @NotNull(message = "Initial balance is required")
    private BigDecimal currentBalance;
}