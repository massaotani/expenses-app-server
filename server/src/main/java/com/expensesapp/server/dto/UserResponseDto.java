package com.expensesapp.server.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.expensesapp.server.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private UUID id;
    private String name;
    private BigDecimal monthlyIncome;
    private BigDecimal investmentPot;
    private BigDecimal monthlyExpenses;

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .monthlyIncome(user.getMonthlyIncome())
                .investmentPot(user.getInvestmentPot())
                .monthlyExpenses(user.getMonthlyExpenses())
                .build();
    }
}
