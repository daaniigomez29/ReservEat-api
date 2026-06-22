package com.restaurant.infrastructure.security.google;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class GoogleAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String clientId;

    public GoogleAudienceValidator(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (clientId == null || clientId.isBlank()) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_configuration", "google.oauth.client-id is not configured", null)
            );
        }

        List<String> audiences = token.getAudience();
        if (audiences != null && audiences.contains(clientId)) {
            return OAuth2TokenValidatorResult.success();
        }

        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "Invalid audience (aud) for Google ID token", null)
        );
    }
}

