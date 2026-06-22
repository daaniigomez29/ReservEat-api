package com.restaurant.domain.valueobject;

public record UserId(Long value) {
    public UserId {
        if (value == null) throw new IllegalArgumentException("UserID cannot be null");
    }
}
