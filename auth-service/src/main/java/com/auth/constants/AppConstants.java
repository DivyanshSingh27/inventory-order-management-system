package com.auth.constants;

public class AppConstants {

    private AppConstants() {}

    // Success messages
    public static final String USER_REGISTERED     = "User registered successfully";
    public static final String LOGIN_SUCCESS       = "Login successful";
    public static final String LOGOUT_SUCCESS      = "Logged out successfully";
    public static final String USER_FETCHED        = "User fetched successfully";

    // Error messages
    public static final String USER_NOT_FOUND      = "User not found with id: ";
    public static final String EMAIL_ALREADY_EXISTS = "Email already registered: ";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String INVALID_TOKEN       = "Invalid or expired token";
    public static final String TOKEN_REQUIRED      = "Authorization token is required";

    // Token
    public static final String TOKEN_PREFIX        = "Bearer ";
    public static final String HEADER_AUTH         = "Authorization";

    // Roles
    public static final String ROLE_USER           = "USER";
    public static final String ROLE_ADMIN          = "ADMIN";
}
