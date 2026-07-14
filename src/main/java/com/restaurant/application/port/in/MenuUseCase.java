package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.CreateMenuCategoryRequest;
import com.restaurant.application.dto.request.CreateMenuItemRequest;
import com.restaurant.application.dto.response.MenuCategoryResponse;
import com.restaurant.application.dto.response.MenuItemResponse;
import com.restaurant.application.dto.response.MenuResponse;
import com.restaurant.domain.model.AuthUser;

public interface MenuUseCase {

    MenuResponse createMenu(Long restaurantId, AuthUser requester);

    MenuResponse getMenuByRestaurant(Long restaurantId);

    MenuCategoryResponse addCategory(CreateMenuCategoryRequest request, AuthUser requester);

    MenuItemResponse addMenuItem(CreateMenuItemRequest request, AuthUser requester);

    void deleteMenuItem(Long itemId, AuthUser requester);

    void deleteCategory(Long categoryId, AuthUser requester);
}
