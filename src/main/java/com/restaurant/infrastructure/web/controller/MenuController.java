package com.restaurant.infrastructure.web.controller;

import com.restaurant.application.dto.request.CreateMenuCategoryRequest;
import com.restaurant.application.dto.request.CreateMenuItemRequest;
import com.restaurant.application.dto.response.MenuCategoryResponse;
import com.restaurant.application.dto.response.MenuItemResponse;
import com.restaurant.application.dto.response.MenuResponse;
import com.restaurant.application.port.in.MenuUseCase;
import com.restaurant.domain.model.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuUseCase menuUseCase;

    @PostMapping("/restaurant/{restaurantId}")
    public ResponseEntity<MenuResponse> createMenu(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuUseCase.createMenu(restaurantId, user.getId().value()));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<MenuResponse> getMenuByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuUseCase.getMenuByRestaurant(restaurantId));
    }

    @PostMapping("/categories")
    public ResponseEntity<MenuCategoryResponse> addCategory(
            @Valid @RequestBody CreateMenuCategoryRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuUseCase.addCategory(request, user.getId().value()));
    }

    @PostMapping("/items")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @Valid @RequestBody CreateMenuItemRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuUseCase.addMenuItem(request, user.getId().value()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal AuthUser user) {
        menuUseCase.deleteMenuItem(itemId, user.getId().value());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal AuthUser user) {
        menuUseCase.deleteCategory(categoryId, user.getId().value());
        return ResponseEntity.noContent().build();
    }
}
