package com.mib.backend.service;

import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RefreshTokenRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
