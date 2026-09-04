package com.expensesapp.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesapp.server.dto.UserResponseDto;
import com.expensesapp.server.dto.UserUpdateRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.service.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.getCurrentUser(authUser));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateCurrentUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserUpdateRequest dto) {
        return ResponseEntity.ok(userService.updateCurrentUser(authUser, dto));
    }
}
