package com.expensesapp.server.service.user;

import com.expensesapp.server.dto.UserResponseDto;
import com.expensesapp.server.model.AuthUser;

public interface UserService {
    UserResponseDto getCurrentUser(AuthUser authUser);
}