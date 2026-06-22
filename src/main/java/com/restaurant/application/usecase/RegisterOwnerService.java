package com.restaurant.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.application.dto.request.RegisterOwnerRequest;
import com.restaurant.application.dto.request.RegisterRequest;
import com.restaurant.application.dto.response.RegistrationResponse;
import com.restaurant.application.port.in.EmailVerificationUseCase;
import com.restaurant.application.port.in.RegisterOwnerUseCase;
import com.restaurant.application.port.in.RestaurantUseCase;
import com.restaurant.domain.exception.EmailAlreadyExistsException;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.GlobalRole;
import com.restaurant.domain.model.RestaurantRole;
import com.restaurant.domain.repository.AuthUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterOwnerService implements RegisterOwnerUseCase{

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantUseCase restaurantUseCase;
    private final EmailVerificationUseCase emailVerificationUseCase;

    @Override
    @Transactional
    public RegistrationResponse registerOwner(RegisterOwnerRequest request) {
        RegisterRequest account = request.getAccount();
        if (userRepository.existsByEmail(account.getEmail())) {
            throw new EmailAlreadyExistsException(account.getEmail());
        }


        AuthUser user = AuthUser.builder()
                .email(account.getEmail())
                .username(account.getUsername())
                .name(account.getName())
                .tlf(account.getTlf())
                .password(passwordEncoder.encode(account.getPassword()))
                .globalRole(GlobalRole.USER)
                .restaurantRole(RestaurantRole.OWNER)
                .emailVerified(false)
                .build();

        AuthUser saved = userRepository.save(user);

        // Atomic: if restaurant creation fails (validation), the user is rolled back too.
        var restaurant = restaurantUseCase.createRestaurant(request.getRestaurant(), saved.getId().value());
        log.info("New owner registered with restaurant (pending verification): {} (restaurantId={})",
                saved.getEmail(), restaurant.getId());

        // No auto-login: the owner must verify their email before logging in.
        emailVerificationUseCase.startVerification(saved);

        return RegistrationResponse.builder()
                .userId(saved.getId().value())
                .email(saved.getEmail())
                .emailVerified(false)
                .message("Registro completado. Revisa tu correo para verificar tu cuenta.")
                .build();
    }
}
