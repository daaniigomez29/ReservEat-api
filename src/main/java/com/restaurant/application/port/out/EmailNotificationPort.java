package com.restaurant.application.port.out;

import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.Reservation;

/**
 * Outbound port for transactional email notifications.
 *
 * <p>Use cases depend on this interface; the concrete delivery mechanism (Resend,
 * SMTP, ...) lives in the infrastructure layer. Implementations are expected to be
 * fire-and-forget: a delivery failure must never propagate back to the caller nor
 * roll back the originating business transaction.
 */
public interface EmailNotificationPort {

    /** Sends the welcome email after a user verifies their account. */
    void sendWelcomeEmail(AuthUser user);

    /** Sends the email-verification link to a freshly registered (or resending) user. */
    void sendVerificationEmail(AuthUser user, String verificationLink);

    /** Confirms a newly created reservation to the booker. */
    void sendReservationConfirmation(Reservation reservation, String restaurantName);

    /** Notifies the booker that their reservation was cancelled. */
    void sendReservationCancellation(Reservation reservation, String restaurantName);
}
