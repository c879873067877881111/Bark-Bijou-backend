package com.smallnine.apiserver.service;

import com.smallnine.apiserver.dto.*;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String username);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);
}
