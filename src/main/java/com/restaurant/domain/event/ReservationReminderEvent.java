package com.restaurant.domain.event;

import com.restaurant.domain.model.Reservation;

public record ReservationReminderEvent(Reservation reservation, String restaurantName) {

}
