package com.restaurant.application.port.in;

import com.restaurant.application.dto.response.FloorPlanResponse;
import com.restaurant.domain.model.AuthUser;

import java.time.LocalDateTime;

public interface FloorPlanUseCase {

    /**
     * Returns the restaurant's table layout together with each table's occupancy
     * state at the given instant. If {@code at} is null, the current time is used.
     * Restricted to the restaurant owner or an admin.
     */
    FloorPlanResponse getFloorPlan(Long restaurantId, LocalDateTime at, AuthUser requester);
}
