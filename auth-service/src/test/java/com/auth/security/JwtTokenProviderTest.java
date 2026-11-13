package com.auth.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String VALID_TEST_SECRET = "testSecretKeyMustBeAtLeast32BytesLongForHS256Validation!";
    private static final long EXPIRATION_MS = 1800000L; // 30 minutes

    @Test
    @DisplayName("Fail fast: throws IllegalStateException when secret is null or empty")
    void failFast_whenSecretIsMissing_throwsIllegalStateException() {
        assertThatThrownBy(() -> new JwtTokenProvider(null, EXPIRATION_MS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");

        assertThatThrownBy(() -> new JwtTokenProvider("", EXPIRATION_MS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    @DisplayName("Fail fast: throws IllegalStateException when secret is shorter than 32 bytes")
    void failFast_whenSecretIsShorterThan32Bytes_throwsIllegalStateException() {
        String shortSecret = "short-secret-under-32-bytes!"; // 28 bytes
        assertThat(shortSecret.getBytes(StandardCharsets.UTF_8).length).isLessThan(32);

        assertThatThrownBy(() -> new JwtTokenProvider(shortSecret, EXPIRATION_MS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    @DisplayName("Token generation: creates valid HS256 token with sub=userId, email, role, and 30m expiry")
    void generateToken_hasExpectedClaimsAnd30MinExpiry() {
        JwtTokenProvider provider = new JwtTokenProvider(VALID_TEST_SECRET, EXPIRATION_MS);

        long userId = 42L;
        String email = "alice@example.com";
        String role = "USER";

        String token = provider.generateToken(userId, email, role);
        assertThat(token).isNotBlank();

        Claims claims = provider.validateAndGetClaims(token);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId));
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.get("role", String.class)).isEqualTo(role);

        long durationMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(durationMs).isEqualTo(EXPIRATION_MS);
    }

    @Test
    @DisplayName("Tampered token: rejects token with invalid signature")
    void validateAndGetClaims_rejectsTamperedToken() {
        JwtTokenProvider provider = new JwtTokenProvider(VALID_TEST_SECRET, EXPIRATION_MS);
        String token = provider.generateToken(1L, "user@example.com", "USER");

        String tamperedToken = token.substring(0, token.length() - 4) + "abcd";

        assertThatThrownBy(() -> provider.validateAndGetClaims(tamperedToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Expired token: rejects token that has passed expiration time")
    void validateAndGetClaims_rejectsExpiredToken() {
        // Negative expiration (-1000ms) to simulate instant expiration
        JwtTokenProvider provider = new JwtTokenProvider(VALID_TEST_SECRET, -1000L);
        String token = provider.generateToken(1L, "expired@example.com", "USER");

        assertThatThrownBy(() -> provider.validateAndGetClaims(token))
                .isInstanceOf(Exception.class);
    }
}
