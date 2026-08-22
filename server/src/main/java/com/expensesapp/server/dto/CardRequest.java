package com.expensesapp.server.dto;

import com.expensesapp.server.model.enums.CardType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardRequest {

    @NotBlank(message = "Card name is required (e.g. Nubank, Chase)")
    private String name;

    @NotNull(message = "Card type is required (CREDIT or DEBIT)")
    private CardType cardType;
}