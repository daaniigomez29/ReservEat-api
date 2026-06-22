package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.CreateMenuCategoryRequest;
import com.restaurant.application.dto.request.CreateMenuItemRequest;
import com.restaurant.application.dto.response.MenuCategoryResponse;
import com.restaurant.application.dto.response.MenuItemResponse;
import com.restaurant.application.dto.response.MenuResponse;

public interface MenuUseCase {

    MenuResponse createMenu(Long restaurantId, Long requesterId);

    MenuResponse getMenuByRestaurant(Long restaurantId);

    MenuCategoryResponse addCategory(CreateMenuCategoryRequest request, Long requesterId);

    MenuItemResponse addMenuItem(CreateMenuItemRequest request, Long requesterId);

    void deleteMenuItem(Long itemId, Long requesterId);

    void deleteCategory(Long categoryId, Long requesterId);
}
