package com.restaurant.infrastructure.config;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Wires the Resend client and enables {@code @Async} so email delivery runs off the
 * request thread. The API key is read from the {@code RESEND_API_KEY} environment
 * variable and must never be hard-coded.
 */
@Configuration
@EnableAsync
public class ResendConfig {

    @Bean
    public Resend resend(@Value("${resend.api-key:}") String apiKey) {
        return new Resend(apiKey);
    }
}
