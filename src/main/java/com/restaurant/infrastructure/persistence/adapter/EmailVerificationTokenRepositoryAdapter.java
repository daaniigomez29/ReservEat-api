package com.restaurant.infrastructure.persistence.adapter;

import com.restaurant.domain.model.EmailVerificationToken;
import com.restaurant.domain.repository.EmailVerificationTokenRepository;
import com.restaurant.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import com.restaurant.infrastructure.persistence.repository.JpaEmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepository {

    private final JpaEmailVerificationTokenRepository jpa;

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        return toDomain(jpa.save(toEntity(token)));
    }

    @Override
    public Optional<EmailVerificationToken> findByToken(String token) {
        return jpa.findByToken(token).map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        jpa.deleteByUserId(userId);
    }

    private EmailVerificationTokenEntity toEntity(EmailVerificationToken token) {
        return EmailVerificationTokenEntity.builder()
                .id(token.getId())
                .token(token.getToken())
                .userId(token.getUserId())
                .expiresAt(token.getExpiresAt())
                .used(token.isUsed())
                .createdAt(token.getCreatedAt())
                .build();
    }

    private EmailVerificationToken toDomain(EmailVerificationTokenEntity entity) {
        return EmailVerificationToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .userId(entity.getUserId())
                .expiresAt(entity.getExpiresAt())
                .used(entity.isUsed())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
