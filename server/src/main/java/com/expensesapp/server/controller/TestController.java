package com.expensesapp.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensesapp.server.model.AuthUser;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/secure")
    // @AuthenticationPrincipal extracts the user identity directly from the
    // validated JWT context
    public ResponseEntity<String> getSecureData(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            return ResponseEntity.status(401).body("Error: Authentication principal mismatch.");
        }

        String displayName = (authUser.getUserProfile() != null) ? authUser.getUserProfile().getName() : "System Administrator";
        String message = String.format(
                "Access Granted! Hello %s. Your JWT signature is valid, and you have successfully passed through the security filter.",
                displayName);
        return ResponseEntity.ok(message);
    }
}