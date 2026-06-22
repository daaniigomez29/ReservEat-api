package com.restaurant.domain.repository;

import com.restaurant.domain.model.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenRepository {

    EmailVerificationToken save(EmailVerificationToken token);

    Optional<EmailVerificationToken> findByToken(String token);

    /** Removes any existing tokens for a user before issuing a fresh one. */
    void deleteByUserId(Long userId);
}
