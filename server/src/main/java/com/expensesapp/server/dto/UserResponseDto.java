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
    private String email;
    private BigDecimal monthlyIncome;
    private BigDecimal investmentPot;
    private BigDecimal monthlyExpenses;
    private String token;

    public static UserResponseDto fromEntity(User user, String email) {
        return fromEntity(user, email, null);
    }

    public static UserResponseDto fromEntity(User user, String email, String token) {

        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(email)
                .monthlyIncome(user.getMonthlyIncome())
                .investmentPot(user.getInvestmentPot())
                .monthlyExpenses(user.getMonthlyExpenses())
                .token(token)
                .build();
    }
}
