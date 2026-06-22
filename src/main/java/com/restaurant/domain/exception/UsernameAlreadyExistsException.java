package com.restaurant.domain.exception;

public class UsernameAlreadyExistsException extends DomainException{
    public UsernameAlreadyExistsException(String username) {
        super("Username already in use: " + username);
    }
    
}
