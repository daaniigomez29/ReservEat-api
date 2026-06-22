package com.restaurant.infrastructure.web.controller;

import com.restaurant.application.dto.response.FloorPlanResponse;
import com.restaurant.application.port.in.FloorPlanUseCase;
import com.restaurant.domain.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/restaurants/{restaurantId}/floor-plan")
@RequiredArgsConstructor
public class FloorPlanController {

    private final FloorPlanUseCase floorPlanUseCase;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FloorPlanResponse> getFloorPlan(
            @PathVariable Long restaurantId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(
                floorPlanUseCase.getFloorPlan(restaurantId, at, user.getId().value()));
    }
}
