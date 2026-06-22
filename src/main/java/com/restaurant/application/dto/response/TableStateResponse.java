package com.restaurant.application.dto.response;

import com.restaurant.domain.model.TableShape;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableStateResponse {

    // Layout
    private Long tableId;
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

    // Occupancy at the queried instant
    private TableStatus status;
    private Long reservationId;       // null when FREE
    private Integer partySize;        // null when FREE
    private String bookerEmail;       // null when FREE
    private LocalDateTime occupiedUntil; // reservation end; null when FREE
}
