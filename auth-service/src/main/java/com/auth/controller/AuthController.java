package com.auth.controller;

import com.auth.constants.AppConstants;
import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.RegisterRequest;
import com.auth.dto.response.ApiResponse;
import com.auth.dto.response.AuthResponse;
import com.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register - email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message(AppConstants.USER_REGISTERED)
                        .data(response)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message(AppConstants.LOGIN_SUCCESS)
                .data(response)
                .build());
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<AuthResponse>> validate(
            @RequestHeader(AppConstants.HEADER_AUTH) String authHeader) {
        log.info("GET /api/v1/auth/validate");
        String token = authHeader.startsWith(AppConstants.TOKEN_PREFIX)
                ? authHeader.substring(7)
                : authHeader;
        AuthResponse response = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Token is valid")
                .data(response)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(AppConstants.HEADER_AUTH) String authHeader) {
        log.info("POST /api/v1/auth/logout");
        String token = authHeader.startsWith(AppConstants.TOKEN_PREFIX)
                ? authHeader.substring(7)
                : authHeader;
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message(AppConstants.LOGOUT_SUCCESS)
                .build());
    }
}
