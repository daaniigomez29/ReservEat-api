package com.restaurant.infrastructure.persistence.repository;

import com.restaurant.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaEmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationTokenEntity, Long> {

    Optional<EmailVerificationTokenEntity> findByToken(String token);

    @Modifying
    @Query("delete from EmailVerificationTokenEntity t where t.userId = :userId")
    void deleteByUserId(Long userId);
}
