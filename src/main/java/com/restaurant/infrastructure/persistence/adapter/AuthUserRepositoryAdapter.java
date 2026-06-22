package com.restaurant.infrastructure.persistence.adapter;

import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.repository.AuthUserRepository;
import com.restaurant.domain.valueobject.UserId;
import com.restaurant.infrastructure.persistence.entity.AuthUserEntity;
import com.restaurant.infrastructure.persistence.repository.JpaAuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUserRepositoryAdapter implements AuthUserRepository {

    private final JpaAuthUserRepository jpa;

    @Override
    public AuthUser save(AuthUser user) {
        AuthUserEntity entity = toEntity(user);
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<AuthUser> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        return jpa.findByUsername(username).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    private AuthUserEntity toEntity(AuthUser user) {
        var builder = AuthUserEntity.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .name(user.getName())
                .tlf(user.getTlf())
                .password(user.getPassword())
                .globalRole(user.getGlobalRole())
                .restaurantRole(user.getRestaurantRole())
                .emailVerified(user.isEmailVerified());

        if(user.getId() != null) {
            builder.id(user.getId().value());
        }

        return builder.build();
    }

    private AuthUser toDomain(AuthUserEntity entity) {
        return AuthUser.builder()
                .id(new UserId(entity.getId()))
                .email(entity.getEmail())
                .username(entity.getUsername())
                .name(entity.getName())
                .tlf(entity.getTlf())
                .password(entity.getPassword())
                .globalRole(entity.getGlobalRole())
                .restaurantRole(entity.getRestaurantRole())
                .emailVerified(entity.isEmailVerified())
                .build();
    }
}
