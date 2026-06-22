package com.restaurant.infrastructure.email;

import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.Reservation;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Builds the HTML bodies for transactional emails. Kept inline for now; if the
 * number/complexity of emails grows, migrate these to Thymeleaf templates under
 * {@code resources/templates/email/}.
 */
final class EmailTemplates {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy, HH:mm'h'", new Locale("es", "ES"));

    private EmailTemplates() {
    }

    static String welcome(AuthUser user) {
        String displayName = (user.getName() != null && !user.getName().isBlank())
                ? user.getName()
                : user.getUsername();

        return layout("""
                <h1 style="margin:0 0 16px;font-size:22px;color:#111827;">¡Bienvenido, %s! 👋</h1>
                <p style="margin:0 0 16px;font-size:15px;line-height:1.6;color:#374151;">
                    Tu cuenta en <strong>Restaurant</strong> se ha creado correctamente.
                    Ya puedes buscar restaurantes y gestionar tus reservas.
                </p>
                <p style="margin:0;font-size:15px;line-height:1.6;color:#374151;">
                    ¡Que aproveche!
                </p>
                """.formatted(escape(displayName)));
    }

    static String verification(AuthUser user, String verificationLink) {
        String displayName = (user.getName() != null && !user.getName().isBlank())
                ? user.getName()
                : user.getUsername();

        return layout("""
                <h1 style="margin:0 0 16px;font-size:22px;color:#111827;">Verifica tu cuenta</h1>
                <p style="margin:0 0 16px;font-size:15px;line-height:1.6;color:#374151;">
                    Hola %s, gracias por registrarte en <strong>Restaurant</strong>.
                    Confirma tu correo para activar tu cuenta:
                </p>
                <p style="margin:0 0 24px;text-align:center;">
                    <a href="%s" style="display:inline-block;background:#111827;color:#ffffff;
                        text-decoration:none;padding:12px 28px;border-radius:8px;font-size:15px;font-weight:600;">
                        Verificar mi correo
                    </a>
                </p>
                <p style="margin:0;font-size:13px;line-height:1.6;color:#6b7280;">
                    Si el botón no funciona, copia y pega este enlace en tu navegador:<br>
                    <a href="%s" style="color:#2563eb;word-break:break-all;">%s</a>
                </p>
                """.formatted(escape(displayName), verificationLink, verificationLink, escape(verificationLink)));
    }

    static String reservationConfirmation(Reservation reservation, String restaurantName) {
        return layout("""
                <h1 style="margin:0 0 16px;font-size:22px;color:#111827;">Reserva confirmada ✅</h1>
                <p style="margin:0 0 16px;font-size:15px;line-height:1.6;color:#374151;">
                    Tu reserva en <strong>%s</strong> está confirmada. Aquí tienes los detalles:
                </p>
                %s
                <p style="margin:16px 0 0;font-size:15px;line-height:1.6;color:#374151;">
                    ¡Te esperamos!
                </p>
                """.formatted(escape(restaurantName), detailsTable(reservation)));
    }

    static String reservationCancellation(Reservation reservation, String restaurantName) {
        return layout("""
                <h1 style="margin:0 0 16px;font-size:22px;color:#111827;">Reserva cancelada</h1>
                <p style="margin:0 0 16px;font-size:15px;line-height:1.6;color:#374151;">
                    Tu reserva en <strong>%s</strong> ha sido cancelada. Detalles de la reserva cancelada:
                </p>
                %s
                <p style="margin:16px 0 0;font-size:15px;line-height:1.6;color:#374151;">
                    Si no has sido tú, ponte en contacto con el restaurante.
                </p>
                """.formatted(escape(restaurantName), detailsTable(reservation)));
    }

    /** Renders the shared reservation details block (date and party size). */
    private static String detailsTable(Reservation reservation) {
        return """
                <table style="width:100%%;border-collapse:collapse;font-size:15px;color:#374151;">
                    <tr>
                        <td style="padding:8px 0;color:#6b7280;">Fecha</td>
                        <td style="padding:8px 0;text-align:right;font-weight:600;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding:8px 0;color:#6b7280;border-top:1px solid #e5e7eb;">Comensales</td>
                        <td style="padding:8px 0;text-align:right;font-weight:600;border-top:1px solid #e5e7eb;">%d</td>
                    </tr>
                </table>
                """.formatted(escape(DATE_FORMAT.format(reservation.getStartDate())), reservation.getPartySize());
    }

    /** Wraps body content in a minimal, email-client-safe layout. */
    private static String layout(String body) {
        return """
                <div style="background:#f3f4f6;padding:24px;font-family:Arial,Helvetica,sans-serif;">
                    <div style="max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;padding:32px;">
                        %s
                    </div>
                    <p style="max-width:520px;margin:16px auto 0;font-size:12px;color:#9ca3af;text-align:center;">
                        Restaurant · Este es un mensaje automático, por favor no respondas a este correo.
                    </p>
                </div>
                """.formatted(body);
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
