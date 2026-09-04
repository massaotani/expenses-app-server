package com.expensesapp.server.service.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.expensesapp.server.dto.UserResponseDto;
import com.expensesapp.server.dto.UserUpdateRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.User;
import com.expensesapp.server.repository.AuthUserRepository;
import com.expensesapp.server.repository.UserRepository;
import com.expensesapp.server.service.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthUserRepository authUserRepository;
    private final JwtService jwtService;

    @Override
    public UserResponseDto getCurrentUser(AuthUser authUser) {
        if (authUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        User user = userRepository.findById(authUser.getUserProfile().getId())
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found with ID: " + authUser.getUserProfile().getId()
        ));

        return UserResponseDto.fromEntity(user, authUser.getEmail());
    }

@Override
public UserResponseDto updateCurrentUser(AuthUser authUser, UserUpdateRequest dto) {
    if (authUser == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
    }

    boolean emailChanged = false;

    // 1. Handle Email Update on AuthUser
    if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(authUser.getEmail())) {
        if (authUserRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken");
        }
        authUser.setEmail(dto.getEmail());
        authUserRepository.save(authUser);
        emailChanged = true;
    }

    // 2. Handle Name Update on User profile
    User user = userRepository.findById(authUser.getUserProfile().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

    if (dto.getName() != null && !dto.getName().isBlank()) {
        user.setName(dto.getName());
        userRepository.save(user);
    }

    // 3. Issue new JWT if email was updated
    String newToken = emailChanged ? jwtService.generateToken(authUser) : null;

    return UserResponseDto.fromEntity(user, authUser.getEmail(), newToken);
}
}
