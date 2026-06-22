package com.restaurant.infrastructure.web.controller;

import com.restaurant.application.port.in.EmailVerificationUseCase;
import com.restaurant.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationUseCase emailVerificationUseCase;

    @Value("${app.frontend.email-verified-url}")
    private String successUrl;

    @Value("${app.frontend.email-verify-error-url}")
    private String errorUrl;

    /**
     * Clicked from the email. Verifies the token and redirects the browser to a
     * frontend page (success or error) rather than returning raw JSON.
     */
    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestParam String token) {
        try {
            emailVerificationUseCase.verifyEmail(token);
            return redirectTo(successUrl);
        } catch (DomainException ex) {
            log.info("Email verification failed: {}", ex.getMessage());
            return redirectTo(errorUrl);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resend(@RequestBody Map<String, String> body) {
        emailVerificationUseCase.resendVerification(body.get("email"));
        return ResponseEntity.accepted().build();
    }

    private ResponseEntity<Void> redirectTo(String url) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }
}
