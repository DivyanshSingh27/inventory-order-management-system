package com.auth.service;

import com.auth.constants.AppConstants;
import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.RegisterRequest;
import com.auth.dto.response.AuthResponse;
import com.auth.entity.User;
import com.auth.exception.AuthException;
import com.auth.repository.UserRepository;
import com.auth.service.impl.AuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private AuthServiceImpl authService;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, new BCryptPasswordEncoder());
    }

    // ─── 1. Role Ignored & Default User Role ─────────────────────────────

    @Test
    @DisplayName("Role test 4(a): Deserializing JSON containing role=ADMIN into RegisterRequest ignores the role")
    void deserialize_jsonWithRole_roleFieldShouldBeIgnored() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
            {
                "name": "Admin Attempter",
                "email": "admin@example.com",
                "password": "validPassword123",
                "role": "ADMIN"
            }
            """;

        RegisterRequest request = mapper.readValue(json, RegisterRequest.class);
        assertThat(request.getName()).isEqualTo("Admin Attempter");
        assertThat(request.getEmail()).isEqualTo("admin@example.com");

        // The RegisterRequest class must NOT declare a 'role' field
        assertThat(Arrays.stream(RegisterRequest.class.getDeclaredFields()).map(Field::getName))
                .as("RegisterRequest must NOT accept or declare a 'role' field")
                .doesNotContain("role");
    }

    @Test
    @DisplayName("Role test 4(b): Service registration asserts saved User role equals default 'USER'")
    void register_shouldAlwaysAssignDefaultUserRole_evenIfRoleProvided() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Attempter");
        request.setEmail("attempter@example.com");
        request.setPassword("password123");

        // If a setter exists on the request, simulate a client attempting to pass a role
        try {
            Method setRoleMethod = RegisterRequest.class.getMethod("setRole", String.class);
            setRoleMethod.invoke(request, "ROLE_ADMIN");
        } catch (NoSuchMethodException ignored) {
        }

        when(userRepository.existsByEmail("attempter@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole())
                .as("User role must strictly be 'USER'")
                .isEqualTo("USER");
    }

    // ─── 2. Password Validation (min = 8, max = 72) ──────────────────────

    @Test
    @DisplayName("Validation: RegisterRequest password must enforce @Size(min = 8, max = 72)")
    void registerRequest_passwordValidation_shouldEnforceMin8AndMax72() {
        RegisterRequest shortPassword = new RegisterRequest();
        shortPassword.setName("John Doe");
        shortPassword.setEmail("john@example.com");
        shortPassword.setPassword("1234567"); // 7 chars (< 8)

        Set<ConstraintViolation<RegisterRequest>> shortViolations = validator.validate(shortPassword);
        assertThat(shortViolations)
                .as("Password with 7 characters must trigger validation violation")
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));

        RegisterRequest longPassword = new RegisterRequest();
        longPassword.setName("John Doe");
        longPassword.setEmail("john@example.com");
        longPassword.setPassword("a".repeat(73)); // 73 chars (> 72)

        Set<ConstraintViolation<RegisterRequest>> longViolations = validator.validate(longPassword);
        assertThat(longViolations)
                .as("Password with 73 characters must trigger validation violation")
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));

        RegisterRequest validPassword = new RegisterRequest();
        validPassword.setName("John Doe");
        validPassword.setEmail("john@example.com");
        validPassword.setPassword("validPass8"); // 10 chars (valid)

        Set<ConstraintViolation<RegisterRequest>> validViolations = validator.validate(validPassword);
        assertThat(validViolations)
                .as("Valid 10-character password must have no password violations")
                .noneMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    // ─── 3. Password Hashing (BCrypt $2 prefix) ──────────────────────────

    @Test
    @DisplayName("Registration: Stored password must be encoded with BCrypt starting with '$2'")
    void register_shouldEncodePasswordWithBCrypt() {
        RegisterRequest request = new RegisterRequest();
        request.setName("BCrypt Test");
        request.setEmail("bcrypt@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("bcrypt@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword())
                .as("Stored password must start with BCrypt identifier '$2'")
                .startsWith("$2");
    }

    // ─── 4. Login: Wrong Password Fails ──────────────────────────────────

    @Test
    @DisplayName("Login: Wrong password fails with AuthException")
    void login_wrongPassword_shouldFailWithAuthException() {
        String email = "user@example.com";
        User user = User.builder()
                .id(3L)
                .name("Normal User")
                .email(email)
                .password("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy") // "password123"
                .active(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("wrongPassword");

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.INVALID_CREDENTIALS);
    }

    // ─── 5. Legacy Hash Upgrade & Failure Cases ──────────────────────────

    @Test
    @DisplayName("Legacy Hash: Wrong password fails with AuthException and save() is NOT called")
    void login_legacyHash_wrongPassword_failsAndDoesNotSave() {
        String email = "legacy_fail@example.com";
        String correctPassword = "password123";
        String legacyHash = computeLegacySha256(correctPassword);

        User legacyUser = User.builder()
                .id(4L)
                .name("Legacy User")
                .email(email)
                .password(legacyHash)
                .active(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(legacyUser));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("wrongPassword");

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage(AppConstants.INVALID_CREDENTIALS);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Legacy Hash: Login succeeds with legacy SHA-256 and gets upgraded to BCrypt")
    void login_legacyHash_loginSucceedsAndGetsUpgradedToBCrypt() {
        String email = "legacy_success@example.com";
        String rawPassword = "password123";
        String legacyHash = computeLegacySha256(rawPassword);

        User legacyUser = User.builder()
                .id(5L)
                .name("Legacy User")
                .email(email)
                .password(legacyHash)
                .active(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(legacyUser));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isNotNull();

        // Condition 2: Assert that userRepository.save() is invoked with a user whose password starts with $2
        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().getPassword())
                .as("userRepository.save() must be invoked with a user whose password starts with '$2'")
                .startsWith("$2");
    }

    // ─── 6. End-to-End: Newly Registered User Can Log In ─────────────────

    @Test
    @DisplayName("Flow: Newly registered user can log in with their credentials")
    void registerAndLogin_newlyRegisteredUserCanLogIn() {
        String email = "newflow@example.com";
        String rawPassword = "password123";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("New Flow");
        registerRequest.setEmail(email);
        registerRequest.setPassword(rawPassword);

        AtomicReference<User> savedUserHolder = new AtomicReference<>();
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(6L);
            savedUserHolder.set(u);
            return u;
        });

        authService.register(registerRequest);

        User savedUser = savedUserHolder.get();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).startsWith("$2");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedUser));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        AuthResponse loginResponse = authService.login(loginRequest);
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getToken()).isNotNull();
    }

    // ─── Helper for tests ────────────────────────────────────────────────

    private String computeLegacySha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
