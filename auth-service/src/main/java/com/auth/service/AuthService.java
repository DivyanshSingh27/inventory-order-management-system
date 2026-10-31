package com.auth.service;

import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.RegisterRequest;
import com.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse validateToken(String token);

    void logout(String token);
}
