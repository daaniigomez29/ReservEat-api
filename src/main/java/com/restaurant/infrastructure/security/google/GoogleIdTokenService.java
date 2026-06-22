package com.restaurant.infrastructure.security.google;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class GoogleIdTokenService {

    private final JwtDecoder googleJwtDecoder;

    public GoogleIdTokenService(JwtDecoder googleJwtDecoder) {
        this.googleJwtDecoder = googleJwtDecoder;
    }

    public GoogleIdTokenClaims verifyAndExtract(String idToken) {
        Jwt jwt = googleJwtDecoder.decode(idToken);

        String email = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        String name = jwt.getClaimAsString("name");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google ID token does not contain email");
        }

        boolean verified = emailVerified != null && emailVerified;
        return new GoogleIdTokenClaims(jwt.getSubject(), email, verified, name);
    }
}

