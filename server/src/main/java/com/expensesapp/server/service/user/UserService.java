package com.expensesapp.server.service.user;

import com.expensesapp.server.dto.UserResponseDto;
import com.expensesapp.server.dto.UserUpdateRequest;
import com.expensesapp.server.model.AuthUser;

public interface UserService {

    UserResponseDto getCurrentUser(AuthUser authUser);

    UserResponseDto updateCurrentUser(AuthUser authUser, UserUpdateRequest dto);
}
