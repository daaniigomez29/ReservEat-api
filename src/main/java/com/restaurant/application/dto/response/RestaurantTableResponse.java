package com.restaurant.application.dto.response;

import com.restaurant.domain.model.TableShape;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableResponse {

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
}
