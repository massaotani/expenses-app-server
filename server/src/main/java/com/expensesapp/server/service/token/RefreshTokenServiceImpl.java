package com.expensesapp.server.service.token;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesapp.server.dto.AuthenticationResponse;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.RefreshToken;
import com.expensesapp.server.repository.RefreshTokenRepository;
import com.expensesapp.server.service.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    // Configurable in application.properties (defaults to 7 days)
    @Value("${application.security.jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(AuthUser authUser) {
        refreshTokenRepository.deleteByAuthUser(authUser);

        RefreshToken refreshToken = RefreshToken.builder()
                .authUser(authUser)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public AuthenticationResponse verifyAndRotate(String requestToken) {
        RefreshToken token = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            refreshTokenRepository.flush();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        AuthUser authUser = token.getAuthUser();

        // Rotate token: delete current token and issue fresh pair
        refreshTokenRepository.delete(token);
        refreshTokenRepository.flush();
        
        RefreshToken newRefreshToken = createRefreshToken(authUser);
        String newAccessToken = jwtService.generateToken(authUser);

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }
}
