package com.restaurant.infrastructure.web.controller;

import com.restaurant.application.dto.request.CreateTableRequest;
import com.restaurant.application.dto.request.UpdateTableRequest;
import com.restaurant.application.dto.response.RestaurantTableResponse;
import com.restaurant.application.port.in.RestaurantTableUseCase;
import com.restaurant.domain.model.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants/{restaurantId}/tables")
@RequiredArgsConstructor
public class RestaurantTableController {

    private final RestaurantTableUseCase tableUseCase;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestaurantTableResponse> create(
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateTableRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tableUseCase.create(restaurantId, request, user));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RestaurantTableResponse>> list(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(tableUseCase.listByRestaurant(restaurantId, user));
    }

    @GetMapping("/{tableId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestaurantTableResponse> getById(
            @PathVariable Long restaurantId,
            @PathVariable Long tableId,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(tableUseCase.getById(restaurantId, tableId, user));
    }

    @PutMapping("/{tableId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestaurantTableResponse> update(
            @PathVariable Long restaurantId,
            @PathVariable Long tableId,
            @Valid @RequestBody UpdateTableRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(tableUseCase.update(restaurantId, tableId, request, user));
    }

    @DeleteMapping("/{tableId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long restaurantId,
            @PathVariable Long tableId,
            @AuthenticationPrincipal AuthUser user) {
        tableUseCase.delete(restaurantId, tableId, user);
        return ResponseEntity.noContent().build();
    }
}
