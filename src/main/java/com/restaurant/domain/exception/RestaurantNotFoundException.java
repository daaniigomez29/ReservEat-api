package com.restaurant.domain.exception;

public class RestaurantNotFoundException extends DomainException {
    public RestaurantNotFoundException(Long id) {
        super("Restaurant not found with id: " + id);
    }
}
