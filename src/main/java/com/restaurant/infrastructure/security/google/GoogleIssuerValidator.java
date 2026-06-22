package com.restaurant.infrastructure.security.google;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URL;
import java.util.Set;

public class GoogleIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private static final Set<String> ALLOWED_ISSUERS = Set.of(
            "https://accounts.google.com",
            "accounts.google.com"
    );

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        URL issuer = token.getIssuer();
        String issuerValue = issuer != null ? issuer.toString() : null;

        if (issuerValue != null && ALLOWED_ISSUERS.contains(issuerValue)) {
            return OAuth2TokenValidatorResult.success();
        }

        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "Invalid issuer (iss) for Google ID token", null)
        );
    }
}

