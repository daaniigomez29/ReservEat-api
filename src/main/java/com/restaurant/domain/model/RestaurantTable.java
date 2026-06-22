package com.restaurant.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {

    // Logical floor-plan canvas size. The frontend maps these units to pixels.
    public static final int PLAN_MAX_WIDTH = 1000;
    public static final int PLAN_MAX_HEIGHT = 1000;

    private Long id;
    private Long restaurantId;
    private String label;
    private int capacity;
    private Integer minCapacity;
    private String zone;
    private TableShape shape;
    private int x;
    private int y;
    private int width;
    private int height;
    private int rotation;
    private boolean active;

    public boolean fits(int partySize) {
        if (!active) return false;
        if (partySize <= 0) return false;
        if (partySize > capacity) return false;
        if (minCapacity != null && partySize < minCapacity) return false;
        return true;
    }

    public void validateInvariants() {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Table label is required");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Table capacity must be positive");
        }
        if (minCapacity != null && (minCapacity < 1 || minCapacity > capacity)) {
            throw new IllegalArgumentException("minCapacity must be between 1 and capacity");
        }
        if (shape == null) {
            throw new IllegalArgumentException("Table shape is required");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Table dimensions must be positive");
        }
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Table coordinates must be non-negative");
        }
        if (x + width > PLAN_MAX_WIDTH || y + height > PLAN_MAX_HEIGHT) {
            throw new IllegalArgumentException("Table is outside the floor-plan bounds");
        }
        if (rotation < 0 || rotation >= 360) {
            throw new IllegalArgumentException("Rotation must be in [0, 360)");
        }
    }
}
