package com.restaurant.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorPlanResponse {

    private Long restaurantId;
    private LocalDateTime queriedAt;
    private int planWidth;
    private int planHeight;
    private List<TableStateResponse> tables;
}
