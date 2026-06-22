package com.restaurant.domain.exception;

public class EmailNotVerifiedException extends DomainException {
    public EmailNotVerifiedException(String email) {
        super("Email not verified: " + email + ". Please check your inbox to verify your account.");
    }
}
