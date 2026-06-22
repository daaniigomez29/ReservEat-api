package com.restaurant.domain.exception;

public class ReservationConflictException extends DomainException {
    public ReservationConflictException(String message) {
        super(message);
    }
}
