package com.restaurant.domain.exception;

public class InvalidVerificationTokenException extends DomainException {
    public InvalidVerificationTokenException() {
        super("The verification link is invalid or has expired");
    }
}
