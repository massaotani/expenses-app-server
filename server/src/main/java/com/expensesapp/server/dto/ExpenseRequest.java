package com.expensesapp.server.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.expensesapp.server.model.enums.ExpenseCategory;
import com.expensesapp.server.model.enums.PaymentType;
import com.expensesapp.server.model.enums.RecurrencePeriod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ExpenseRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Value is required")
    @Positive(message = "Expense amount must be greater than zero")
    private BigDecimal value;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @NotNull(message = "Payment type selection is required (CREDIT, DEBIT, or CASH)")
    // private UUID paymentMethodId;
    private PaymentType paymentType;

    private boolean isPaid;

    @NotNull(message = "Recurrence period is required (use NONE if one-time)")
    private RecurrencePeriod recurrencePeriod;
}