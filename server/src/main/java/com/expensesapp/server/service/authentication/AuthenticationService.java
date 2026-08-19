package com.expensesapp.server.service.authentication;

import com.expensesapp.server.dto.AuthenticationResponse;
import com.expensesapp.server.dto.LoginRequest;
import com.expensesapp.server.dto.RegisterRequest;
import com.expensesapp.server.dto.TokenRefreshRequest;

public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(LoginRequest request);

    AuthenticationResponse refreshToken(TokenRefreshRequest request);
}
