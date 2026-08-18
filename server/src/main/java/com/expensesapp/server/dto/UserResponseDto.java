package com.expensesapp.server.dto;

import com.expensesapp.server.model.User;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String name,
    BigDecimal monthlyIncome,
    BigDecimal investmentPot,
    BigDecimal monthlyExpenses
) {
    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getName(),
            user.getMonthlyIncome(),
            user.getInvestmentPot(),
            user.getMonthlyExpenses()
        );
    }
}