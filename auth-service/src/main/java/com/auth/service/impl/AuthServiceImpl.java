package com.auth.service.impl;

import com.auth.constants.AppConstants;
import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.RegisterRequest;
import com.auth.dto.response.AuthResponse;
import com.auth.entity.User;
import com.auth.exception.AuthException;
import com.auth.exception.ResourceNotFoundException;
import com.auth.repository.UserRepository;
import com.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException(
                AppConstants.EMAIL_ALREADY_EXISTS + request.getEmail());
        }

        String token = generateToken();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(simpleHash(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : AppConstants.ROLE_USER)
                .token(token)
                .tokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        User saved = userRepository.save(user);
        log.info("User registered with id: {}", saved.getId());

        return mapToResponse(saved, AppConstants.USER_REGISTERED);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(AppConstants.INVALID_CREDENTIALS));

        if (!simpleHash(request.getPassword()).equals(user.getPassword())) {
            throw new AuthException(AppConstants.INVALID_CREDENTIALS);
        }

        if (!user.getActive()) {
            throw new AuthException("Account is deactivated");
        }

        String token = generateToken();
        user.setToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        log.info("Login successful for user id: {}", user.getId());
        return mapToResponse(user, AppConstants.LOGIN_SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse validateToken(String token) {
        User user = userRepository.findByToken(token)
                .orElseThrow(() -> new AuthException(AppConstants.INVALID_TOKEN));

        if (user.getTokenExpiry() == null ||
                user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AuthException(AppConstants.INVALID_TOKEN);
        }

        return mapToResponse(user, "Token is valid");
    }

    @Override
    @Transactional
    public void logout(String token) {
        User user = userRepository.findByToken(token)
                .orElseThrow(() -> new AuthException(AppConstants.INVALID_TOKEN));

        user.setToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
        log.info("User logged out: {}", user.getEmail());
    }

    // ─── Helpers ────────────────────────────────────────

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    private String simpleHash(String input) {
        try {
            java.security.MessageDigest md =
                java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private AuthResponse mapToResponse(User user, String message) {
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(user.getToken())
                .message(message)
                .build();
    }
}
