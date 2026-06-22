package com.restaurant.infrastructure.security.config;

import com.restaurant.infrastructure.security.google.GoogleAudienceValidator;
import com.restaurant.infrastructure.security.google.GoogleIssuerValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class GoogleJwtDecoderConfig {

    @Bean
    public JwtDecoder googleJwtDecoder(
            @Value("${google.oauth.jwk-set-uri}") String jwkSetUri,
            @Value("${google.oauth.client-id}") String clientId
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new GoogleIssuerValidator(),
                new GoogleAudienceValidator(clientId)
        );

        decoder.setJwtValidator(validator);
        return decoder;
    }
}

