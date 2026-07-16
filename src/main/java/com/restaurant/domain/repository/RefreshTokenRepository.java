package com.restaurant.domain.repository;

import com.restaurant.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Invalidates a single session (rotation or logout). No-op if absent. */
    void deleteByTokenHash(String tokenHash);
}
