package com.expensesapp.server.config;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.repository.AuthUserRepository;
import com.expensesapp.server.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// This component intercepts incoming requests, reads the authorization headers, handles parsing verification, and establishes the request's context.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthUserRepository authUserRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userIdStr = jwtService.extractUserId(jwt);

            if (userIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UUID userId = UUID.fromString(userIdStr);
                AuthUser authUser = authUserRepository.findById(userId).orElse(null);

                if (authUser != null && jwtService.isTokenValid(jwt, authUser)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            authUser,
                            null,
                            authUser.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // If the token is expired or malformed, continue down the filter chain as anonymous.
            // Downstream security rules will return a proper 401 Unauthorized if the path is protected.
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
