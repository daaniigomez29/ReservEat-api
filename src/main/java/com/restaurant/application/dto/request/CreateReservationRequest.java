package com.restaurant.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    @NotNull(message = "Restaurant id is required")
    private Long restaurantId;

    @NotNull(message = "Start date is required")
    @Future(message = "Reservation must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @Min(value = 1, message = "Party size must be at least 1")
    @Max(value = 100, message = "Party size cannot exceed 100")
    private int partySize;

    // Email for guests without account; if null, authenticated user's email is used
    @Email
    private String bookerEmail;
}
