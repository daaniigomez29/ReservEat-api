package com.restaurant.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Single-use, time-limited token that proves ownership of an email address.
 * Issued at registration (and on resend); consumed by the verification endpoint.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {

    private Long id;
    private String token;
    private Long userId;
    private LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /** A token can be redeemed only once and only before it expires. */
    public boolean isUsable() {
        return !this.used && !isExpired();
    }

    public void markUsed() {
        this.used = true;
    }
}
