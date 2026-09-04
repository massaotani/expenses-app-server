package com.expensesapp.server.service.authentication;

import com.expensesapp.server.dto.AuthenticationResponse;
import com.expensesapp.server.dto.ChangePasswordRequest;
import com.expensesapp.server.dto.LoginRequest;
import com.expensesapp.server.dto.RegisterRequest;
import com.expensesapp.server.dto.TokenRefreshRequest;
import com.expensesapp.server.model.AuthUser;

public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(LoginRequest request);

    AuthenticationResponse refreshToken(TokenRefreshRequest request);

    void changePassword(AuthUser authUser, ChangePasswordRequest request);
}
