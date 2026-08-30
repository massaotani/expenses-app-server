package com.expensesapp.server.service.authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import com.expensesapp.server.model.MonthlyBalance;
import com.expensesapp.server.model.RefreshToken;
import com.expensesapp.server.model.User;
import com.expensesapp.server.model.enums.AccountRole;
import com.expensesapp.server.repository.AuthUserRepository;
import com.expensesapp.server.repository.MonthlyBalanceRepository;
import com.expensesapp.server.service.JwtService;
import com.expensesapp.server.service.token.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final MonthlyBalanceRepository monthlyBalanceRepository;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email address is already registered");
        }

        User coreProfile = null;

        if (request.getAccountRole() != AccountRole.ADMIN) {
            coreProfile = User.builder()
                    .name(request.getName())
                    .monthlyIncome(request.getMonthlyIncome() != null ? request.getMonthlyIncome() : BigDecimal.ZERO)
                    .investmentPot(BigDecimal.ZERO)
                    .build();
        }

        AuthUser authUser = AuthUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getAccountRole())
                .userProfile(coreProfile)
                .build();

        authUser = repository.save(authUser);

        if (authUser.getUserProfile() != null) {
            LocalDateTime now = LocalDateTime.now();
            BigDecimal initialIncome = authUser.getUserProfile().getMonthlyIncome();

            MonthlyBalance initialBalance = MonthlyBalance.builder()
                    .user(authUser.getUserProfile())
                    .year(now.getYear())
                    .month(now.getMonthValue())
                    .income(initialIncome)
                    .totalExpenses(BigDecimal.ZERO)
                    .savings(initialIncome)
                    .build();

            monthlyBalanceRepository.save(initialBalance);
        }

        String jwtToken = jwtService.generateToken(authUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authUser);
        return AuthenticationResponse.builder().token(jwtToken).refreshToken(refreshToken.getToken()).build();
    }

    @Override
    @Transactional
    public AuthenticationResponse authenticate(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        AuthUser user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"));

        String jwtToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthenticationResponse.builder().token(jwtToken).refreshToken(refreshToken.getToken()).build();
    }

    @Override
    public AuthenticationResponse refreshToken(TokenRefreshRequest request) {
        return refreshTokenService.verifyAndRotate(request.getRefreshToken());
    }
}