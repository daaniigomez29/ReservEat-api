package com.restaurant.application.port.in;

import com.restaurant.domain.model.AuthUser;

public interface EmailVerificationUseCase {

    /** Issues a fresh token for the user and emails the verification link. */
    void startVerification(AuthUser user);

    /** Validates the token, marks the account verified and welcomes the user. */
    void verifyEmail(String token);

    /** Re-issues and re-sends the verification email for an unverified account. */
    void resendVerification(String email);
}
