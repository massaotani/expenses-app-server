package com.expensesapp.server.service.token;

import com.expensesapp.server.dto.AuthenticationResponse;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(AuthUser authUser);

    AuthenticationResponse verifyAndRotate(String requestToken);

}
