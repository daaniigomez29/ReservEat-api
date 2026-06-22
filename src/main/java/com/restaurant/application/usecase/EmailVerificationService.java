package com.restaurant.application.usecase;

import com.restaurant.application.port.in.EmailVerificationUseCase;
import com.restaurant.application.port.out.EmailNotificationPort;
import com.restaurant.domain.exception.InvalidVerificationTokenException;
import com.restaurant.domain.exception.UserNotFoundException;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.EmailVerificationToken;
import com.restaurant.domain.repository.AuthUserRepository;
import com.restaurant.domain.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService implements EmailVerificationUseCase {

    private final AuthUserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailNotificationPort emailNotificationPort;

    @Value("${app.verification.token-ttl-hours:24}")
    private long tokenTtlHours;

    @Value("${app.verification.verify-base-url}")
    private String verifyBaseUrl;

    @Override
    @Transactional
    public void startVerification(AuthUser user) {
        // One active token per user: drop any previous one before issuing a new link.
        tokenRepository.deleteByUserId(user.getId().value());

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .userId(user.getId().value())
                .expiresAt(LocalDateTime.now().plusHours(tokenTtlHours))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        tokenRepository.save(token);

        String link = UriComponentsBuilder.fromUriString(verifyBaseUrl)
                .queryParam("token", token.getToken())
                .toUriString();

        emailNotificationPort.sendVerificationEmail(user, link);
        log.info("Verification email issued for {}", user.getEmail());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken stored = tokenRepository.findByToken(token)
                .orElseThrow(InvalidVerificationTokenException::new);

        if (!stored.isUsable()) {
            throw new InvalidVerificationTokenException();
        }

        AuthUser user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(stored.getUserId())));

        user.setEmailVerified(true);
        userRepository.save(user);

        stored.markUsed();
        tokenRepository.save(stored);

        log.info("Email verified for {}", user.getEmail());
        emailNotificationPort.sendWelcomeEmail(user);
    }

    @Override
    @Transactional
    public void resendVerification(String email) {
        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.isEmailVerified()) {
            log.info("Resend verification skipped: {} is already verified", email);
            return;
        }

        startVerification(user);
    }
}
