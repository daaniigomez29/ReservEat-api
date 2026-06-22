package com.restaurant.domain.exception;

public class ReservationNotFoundException extends DomainException {
    public ReservationNotFoundException(Long id) {
        super("Reservation not found with id: " + id);
    }
}
