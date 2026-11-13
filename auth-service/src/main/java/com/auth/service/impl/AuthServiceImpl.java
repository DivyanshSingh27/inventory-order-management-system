package com.auth.service.impl;

import com.auth.constants.AppConstants;
import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.RegisterRequest;
import com.auth.dto.response.AuthResponse;
import com.auth.entity.User;
import com.auth.exception.AuthException;
import com.auth.repository.UserRepository;
import com.auth.security.JwtTokenProvider;
import com.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException(
                AppConstants.EMAIL_ALREADY_EXISTS + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AppConstants.ROLE_USER)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered with id: {}", saved.getId());

        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getEmail(), saved.getRole());
        return mapToResponse(saved, token, AppConstants.USER_REGISTERED);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(AppConstants.INVALID_CREDENTIALS));

        String rawPassword = request.getPassword();
        String storedHash = user.getPassword();

        if (isBcryptHash(storedHash)) {
            if (!passwordEncoder.matches(rawPassword, storedHash)) {
                throw new AuthException(AppConstants.INVALID_CREDENTIALS);
            }
        } else if (verifyLegacyHash(rawPassword, storedHash)) {
            // Transparently upgrade legacy SHA-256 hash to BCrypt on successful login
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        } else {
            throw new AuthException(AppConstants.INVALID_CREDENTIALS);
        }

        if (!user.getActive()) {
            throw new AuthException("Account is deactivated");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        log.info("Login successful for user id: {}", user.getId());
        return mapToResponse(user, token, AppConstants.LOGIN_SUCCESS);
    }

    @Override
    public AuthResponse validateToken(String token) {
        try {
            Claims claims = jwtTokenProvider.validateAndGetClaims(token);
            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            return AuthResponse.builder()
                    .id(userId)
                    .email(email)
                    .role(role)
                    .token(token)
                    .message("Token is valid")
                    .build();
        } catch (Exception e) {
            throw new AuthException(AppConstants.INVALID_TOKEN);
        }
    }

    @Override
    public void logout(String token) {
        // Documented no-op for stateless JWT authentication:
        // Tokens are cryptographically validated statelessly until expiration;
        // client is responsible for discarding the token on logout.
        log.info("Logout request received; client discards stateless JWT");
    }

    // ─── Helpers ────────────────────────────────────────

    private boolean isBcryptHash(String hash) {
        return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$") || hash.startsWith("$2$"));
    }

    private boolean verifyLegacyHash(String rawPassword, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        String computedHash = simpleHash(rawPassword);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
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

    private AuthResponse mapToResponse(User user, String token, String message) {
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .message(message)
                .build();
    }
}
