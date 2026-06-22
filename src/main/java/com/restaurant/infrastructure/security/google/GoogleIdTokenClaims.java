package com.restaurant.infrastructure.security.google;

public record GoogleIdTokenClaims(
        String subject,
        String email,
        boolean emailVerified,
        String name
) {
}

