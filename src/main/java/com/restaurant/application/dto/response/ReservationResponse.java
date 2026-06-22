package com.restaurant.application.dto.response;

import com.restaurant.domain.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long id;
    private Long restaurantId;
    private String restaurantName;
    // Table fields are populated only in owner-facing views; omitted (null) for customers.
    private Long tableId;
    private String tableLabel;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int partySize;
    private String bookerEmail;
    private ReservationStatus status;
    private LocalDateTime createdAt;
}
