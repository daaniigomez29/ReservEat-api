package com.restaurant.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A persisted, revocable refresh-token session. One row represents one active
 * login on one device. The raw token never touches the database: only its
 * SHA-256 hash ({@code tokenHash}) is stored, so a DB leak cannot be replayed.
 * Logout deletes the row; refresh rotates it (old row deleted, new row saved).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private Long id;
    private String tokenHash;
    private Long userId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
