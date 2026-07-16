package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.LoginRequest;
import com.restaurant.application.dto.request.GoogleLoginRequest;
import com.restaurant.application.dto.request.RegisterRequest;
import com.restaurant.application.dto.response.AuthResponse;
import com.restaurant.application.dto.response.RegistrationResponse;

public interface AuthUseCase {

    RegistrationResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse loginWithGoogle(GoogleLoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    /** Invalidates the given refresh token server-side (logout). Idempotent. */
    void logout(String refreshToken);
}
