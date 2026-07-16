package com.restaurant.infrastructure.web.controller;

import com.restaurant.application.dto.request.GoogleLoginRequest;
import com.restaurant.application.dto.request.LoginRequest;
import com.restaurant.application.dto.request.LogoutRequest;
import com.restaurant.application.dto.request.RegisterOwnerRequest;
import com.restaurant.application.dto.request.RegisterRequest;
import com.restaurant.application.dto.response.AuthResponse;
import com.restaurant.application.dto.response.RegistrationResponse;
import com.restaurant.application.port.in.AuthUseCase;
import com.restaurant.application.port.in.RegisterOwnerUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final RegisterOwnerUseCase registerOwnerUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authUseCase.register(request));
    }

    @PostMapping("/register/owner")
    public ResponseEntity<RegistrationResponse> registerOwner(@Valid @RequestBody RegisterOwnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerOwnerUseCase.registerOwner(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authUseCase.loginWithGoogle(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(authUseCase.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authUseCase.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
