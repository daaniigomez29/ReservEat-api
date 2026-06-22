package com.restaurant.domain.event;

import com.restaurant.domain.model.Reservation;

public record ReservationCreatedEvent(Reservation reservation, String restaurantName) {
    
}
