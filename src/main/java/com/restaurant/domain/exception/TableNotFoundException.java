package com.restaurant.domain.exception;

public class TableNotFoundException extends DomainException {
    public TableNotFoundException(Long id) {
        super("Table not found with id: " + id);
    }
}
