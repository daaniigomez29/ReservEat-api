package com.restaurant.infrastructure.email;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.restaurant.application.port.out.EmailNotificationPort;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Resend-backed implementation of {@link EmailNotificationPort}.
 *
 * <p>Every send runs asynchronously and swallows delivery failures (logging them)
 * so a mail problem never breaks the business flow that triggered it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResendEmailAdapter implements EmailNotificationPort {

    private final Resend resend;

    @Value("${resend.from}")
    private String from;

    @Async
    @Override
    public void sendWelcomeEmail(AuthUser user) {
        send(user.getEmail(), "¡Bienvenido a Restaurant!", EmailTemplates.welcome(user));
    }

    @Async
    @Override
    public void sendVerificationEmail(AuthUser user, String verificationLink) {
        send(user.getEmail(), "Verifica tu cuenta en Restaurant",
                EmailTemplates.verification(user, verificationLink));
    }

    @Async
    @Override
    public void sendReservationConfirmation(Reservation reservation, String restaurantName) {
        send(reservation.getBookerEmail(),
                "Reserva confirmada en " + restaurantName,
                EmailTemplates.reservationConfirmation(reservation, restaurantName));
    }

    @Async
    @Override
    public void sendReservationCancellation(Reservation reservation, String restaurantName) {
        send(reservation.getBookerEmail(),
                "Reserva cancelada en " + restaurantName,
                EmailTemplates.reservationCancellation(reservation, restaurantName));
    }

    @Async
    @Override
    public void sendReservationReminder(Reservation reservation, String restaurantName) {
        send(reservation.getBookerEmail(),
                "Recordatorio de tu reserva en " + restaurantName,
                EmailTemplates.reservationReminder(reservation, restaurantName));
    }

    private void send(String to, String subject, String html) {
        if (to == null || to.isBlank()) {
            log.warn("Skipping email '{}': no recipient address", subject);
            return;
        }
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email '{}' sent to {} (id={})", subject, to, response.getId());
        } catch (ResendException e) {
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }
}
