package com.restaurant.domain.model;

public enum ReservationStatus {
    PENDING,    // legacy: awaiting confirmation (removed from new reservations in step 6)
    CONFIRMED,
    SEATED,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    EXPIRED
}
