package com.expensesapp.server.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    private String name;

    @Email(message = "Invalid email format")
    private String email;
}
