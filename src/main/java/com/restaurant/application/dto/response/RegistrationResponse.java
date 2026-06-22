package com.restaurant.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Returned after registration. No auth tokens are issued: the account stays
 * unverified and login is blocked until the user confirms their email.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    private Long userId;
    private String email;
    private boolean emailVerified;
    private String message;
}
