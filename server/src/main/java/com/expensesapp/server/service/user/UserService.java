package com.expensesapp.server.service.user;

import com.expensesapp.server.dto.UserResponseDto;

public interface UserService {
    UserResponseDto getCurrentUser();
}