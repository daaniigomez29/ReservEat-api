package com.restaurant.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Owner onboarding payload: creates the user account and its first restaurant in
 * a single atomic step. The OWNER role is assigned by the server, never requested
 * by the client.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterOwnerRequest {

    @NotNull(message = "Account data is required")
    @Valid
    private RegisterRequest account;

    @NotNull(message = "Restaurant data is required")
    @Valid
    private CreateRestaurantRequest restaurant;
}
