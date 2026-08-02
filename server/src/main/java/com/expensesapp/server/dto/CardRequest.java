package com.expensesapp.server.dto;

import java.math.BigDecimal;

import com.expensesapp.server.model.enums.CardType;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardRequest {
    @NotNull(message = "Card name is required")
    private String name;

    @NotNull(message = "Card type (CREDIT or DEBIT) is required")
    private CardType cardType;

    private BigDecimal currentBalance = BigDecimal.ZERO;
}