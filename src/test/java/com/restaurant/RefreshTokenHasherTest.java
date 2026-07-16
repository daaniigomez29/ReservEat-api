package com.restaurant;

import com.restaurant.infrastructure.security.RefreshTokenHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenHasherTest {

    private final RefreshTokenHasher hasher = new RefreshTokenHasher();

    @Test
    @DisplayName("Hashing the same token is deterministic and yields a 64-char hex SHA-256")
    void hashIsDeterministicAndHex64() {
        String first = hasher.hash("a-very-long-random-refresh-token");
        String second = hasher.hash("a-very-long-random-refresh-token");

        assertThat(first).isEqualTo(second)
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    @Test
    @DisplayName("Different tokens produce different hashes")
    void differentInputsProduceDifferentHashes() {
        assertThat(hasher.hash("token-a")).isNotEqualTo(hasher.hash("token-b"));
    }
}
