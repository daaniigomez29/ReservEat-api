package com.restaurant.infrastructure.persistence.adapter;

import com.restaurant.domain.model.RefreshToken;
import com.restaurant.domain.repository.RefreshTokenRepository;
import com.restaurant.infrastructure.persistence.entity.RefreshTokenEntity;
import com.restaurant.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpa;

    @Override
    public RefreshToken save(RefreshToken token) {
        return toDomain(jpa.save(toEntity(token)));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByTokenHash(String tokenHash) {
        jpa.deleteByTokenHash(tokenHash);
    }

    private RefreshTokenEntity toEntity(RefreshToken token) {
        return RefreshTokenEntity.builder()
                .id(token.getId())
                .tokenHash(token.getTokenHash())
                .userId(token.getUserId())
                .expiresAt(token.getExpiresAt())
                .createdAt(token.getCreatedAt())
                .build();
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return RefreshToken.builder()
                .id(entity.getId())
                .tokenHash(entity.getTokenHash())
                .userId(entity.getUserId())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
