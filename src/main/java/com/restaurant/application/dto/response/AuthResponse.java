package com.restaurant.application.dto.response;

import com.restaurant.domain.model.GlobalRole;
import com.restaurant.domain.model.RestaurantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String username;
    private GlobalRole globalRole;
    private RestaurantRole restaurantRole;
    // Only set on owner registration (the just-created restaurant); omitted otherwise.
    private Long restaurantId;
}
