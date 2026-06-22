package com.restaurant.domain.repository;

import com.restaurant.domain.model.AuthUser;

import java.util.Optional;

public interface AuthUserRepository {

    AuthUser save(AuthUser user);

    Optional<AuthUser> findById(Long id);

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void deleteById(Long id);
}
