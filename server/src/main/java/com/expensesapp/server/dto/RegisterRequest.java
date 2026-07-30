package com.expensesapp.server.dto;

import java.math.BigDecimal;

import com.expensesapp.server.model.enums.AccountRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotNull(message = "Initial monthly income is mandatory")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Account role is mandatory")
    private AccountRole accountRole = AccountRole.NORMAL;
}