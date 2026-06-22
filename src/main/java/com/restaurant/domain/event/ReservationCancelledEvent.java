package com.restaurant.domain.event;

import com.restaurant.domain.model.Reservation;

public record ReservationCancelledEvent(Reservation reservation, String restaurantName) {

}
