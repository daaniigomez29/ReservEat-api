package com.restaurant.infrastructure.persistence.repository;

import com.restaurant.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaRefreshTokenRepository
        extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from RefreshTokenEntity t where t.tokenHash = :tokenHash")
    void deleteByTokenHash(String tokenHash);
}
