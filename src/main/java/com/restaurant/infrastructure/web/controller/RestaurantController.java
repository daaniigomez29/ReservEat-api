package com.restaurant.infrastructure.web.controller;

import com.restaurant.application.dto.request.CreateRestaurantRequest;
import com.restaurant.application.dto.response.RestaurantResponse;
import com.restaurant.application.port.in.RestaurantUseCase;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantUseCase restaurantUseCase;

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(
            @Valid @RequestBody CreateRestaurantRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantUseCase.createRestaurant(request, user.getId().value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantUseCase.getRestaurantById(id));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAll() {
        return ResponseEntity.ok(restaurantUseCase.getAllRestaurants());
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) CuisineType cuisineType,
            @RequestParam(required = false) DietaryOption dietaryOption,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(restaurantUseCase.searchRestaurants(
                name, city, province, cuisineType, dietaryOption, maxPrice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateRestaurantRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(restaurantUseCase.updateRestaurant(id, request, user.getId().value()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser user) {
        restaurantUseCase.deleteRestaurant(id, user.getId().value());
        return ResponseEntity.noContent().build();
    }
}
