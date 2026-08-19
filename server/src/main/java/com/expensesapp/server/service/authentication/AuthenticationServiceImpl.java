package com.expensesapp.server.service.authentication;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesapp.server.dto.AuthenticationResponse;
import com.expensesapp.server.dto.LoginRequest;
import com.expensesapp.server.dto.RegisterRequest;
import com.expensesapp.server.dto.TokenRefreshRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.RefreshToken;
import com.expensesapp.server.model.User;
import com.expensesapp.server.model.enums.AccountRole;
import com.expensesapp.server.repository.AuthUserRepository;
import com.expensesapp.server.service.JwtService;
import com.expensesapp.server.service.token.RefreshTokenService;

import lombok.RequiredArgsConstructor;

// Handles processing profiles, encrypting passwords securely using BCrypt, and returning runtime tracking tokens.
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email address is already registered");
        }

        User coreProfile = null;

        // Only generate a financial ledger profile if the account is NOT an admin
        if (request.getAccountRole() != AccountRole.ADMIN) {
            coreProfile = User.builder()
                    .name(request.getName())
                    .monthlyIncome(request.getMonthlyIncome())
                    .investmentPot(BigDecimal.ZERO)
                    .build();
        }

        AuthUser authUser = AuthUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getAccountRole()) // e.g., AccountRole.NORMAL, ENTERPRISE, or ADMIN
                .userProfile(coreProfile)
                .build();

        authUser = repository.save(authUser);

        String jwtToken = jwtService.generateToken(authUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authUser);
        return AuthenticationResponse.builder().token(jwtToken).refreshToken(refreshToken.getToken()).build();
    }

    @Override
    public AuthenticationResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        AuthUser user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        String jwtToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return AuthenticationResponse.builder().token(jwtToken).refreshToken(refreshToken.getToken()).build();
    }

    @Override
    public AuthenticationResponse refreshToken(TokenRefreshRequest request) {
        return refreshTokenService.verifyAndRotate(request.getRefreshToken());
    }
}
