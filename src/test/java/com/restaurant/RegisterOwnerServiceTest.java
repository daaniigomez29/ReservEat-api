package com.restaurant;

import com.restaurant.application.dto.request.CreateRestaurantRequest;
import com.restaurant.application.dto.request.RegisterOwnerRequest;
import com.restaurant.application.dto.request.RegisterRequest;
import com.restaurant.application.dto.response.RegistrationResponse;
import com.restaurant.application.dto.response.RestaurantResponse;
import com.restaurant.application.port.in.EmailVerificationUseCase;
import com.restaurant.application.port.in.RestaurantUseCase;
import com.restaurant.application.usecase.RegisterOwnerService;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.GlobalRole;
import com.restaurant.domain.model.RestaurantRole;
import com.restaurant.domain.repository.AuthUserRepository;
import com.restaurant.domain.valueobject.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterOwnerServiceTest {

    @Mock private AuthUserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RestaurantUseCase restaurantUseCase;
    @Mock private EmailVerificationUseCase emailVerificationUseCase;

    @InjectMocks private RegisterOwnerService registerOwnerService;

    @Test
    @DisplayName("registerOwner asigna rol OWNER server-side, crea el restaurante atómicamente y deja la cuenta sin verificar")
    void registerOwner_createsOwnerUserAndRestaurant() {
        RegisterRequest account = RegisterRequest.builder()
                .email("owner@test.com").username("owner1").name("Owner Uno")
                .password("password123").tlf("600111222")
                .build();
        CreateRestaurantRequest restaurant = CreateRestaurantRequest.builder()
                .name("Resto").email("resto@test.com").tlf("600555666")
                .size(10).cuisineType(CuisineType.SPANISH)
                .street("Calle 1").city("Madrid").province("Madrid")
                .lat(new BigDecimal("40.4")).lon(new BigDecimal("-3.7"))
                .build();
        RegisterOwnerRequest request = RegisterOwnerRequest.builder()
                .account(account).restaurant(restaurant)
                .build();

        when(userRepository.existsByEmail("owner@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> {
            AuthUser u = inv.getArgument(0);
            u.setId(new UserId(1L));
            return u;
        });
        when(restaurantUseCase.createRestaurant(any(), anyLong()))
                .thenReturn(RestaurantResponse.builder().id(55L).build());

        RegistrationResponse response = registerOwnerService.registerOwner(request);

        // El usuario se guarda como USER global + OWNER de restaurante (rol fijado por el servidor)
        // y sin verificar todavía.
        ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getGlobalRole()).isEqualTo(GlobalRole.USER);
        assertThat(userCaptor.getValue().getRestaurantRole()).isEqualTo(RestaurantRole.OWNER);
        assertThat(userCaptor.getValue().isEmailVerified()).isFalse();

        // El restaurante se crea con el id del nuevo owner.
        verify(restaurantUseCase).createRestaurant(eq(restaurant), eq(1L));

        // Se dispara la verificación por email y NO se hace auto-login (sin tokens).
        verify(emailVerificationUseCase).startVerification(any(AuthUser.class));
        assertThat(response.getEmail()).isEqualTo("owner@test.com");
        assertThat(response.isEmailVerified()).isFalse();
    }
}
