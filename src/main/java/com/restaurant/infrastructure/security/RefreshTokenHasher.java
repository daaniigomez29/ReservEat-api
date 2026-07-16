package com.restaurant.infrastructure.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashes refresh tokens before they are stored or looked up. Refresh tokens are
 * high-value credentials, so — like passwords — only their hash lives in the DB.
 * SHA-256 (no salt) is enough here: the token is a long, random, high-entropy
 * JWT, so it is not vulnerable to the dictionary attacks that force salting on
 * user-chosen passwords, and an unsalted digest keeps lookups a simple equality
 * match on {@code token_hash}.
 */
@Component
public class RefreshTokenHasher {

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JLS; this branch is effectively unreachable.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
