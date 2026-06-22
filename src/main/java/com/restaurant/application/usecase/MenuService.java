package com.restaurant.application.usecase;

import com.restaurant.application.dto.request.CreateMenuCategoryRequest;
import com.restaurant.application.dto.request.CreateMenuItemRequest;
import com.restaurant.application.dto.response.MenuCategoryResponse;
import com.restaurant.application.dto.response.MenuItemResponse;
import com.restaurant.application.dto.response.MenuResponse;
import com.restaurant.application.port.in.MenuUseCase;
import com.restaurant.domain.exception.DomainException;
import com.restaurant.domain.exception.RestaurantNotFoundException;
import com.restaurant.domain.model.*;
import com.restaurant.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService implements MenuUseCase {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuthUserRepository userRepository;

    @Override
    @Transactional
    public MenuResponse createMenu(Long restaurantId, Long requesterId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        assertOwnerOrAdmin(restaurant, requesterId);

        Menu menu = Menu.builder()
                .restaurantId(restaurantId)
                .updatedAt(Instant.now())
                .build();

        Menu saved = menuRepository.save(menu);
        return toMenuResponse(saved, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getMenuByRestaurant(Long restaurantId) {
        List<Menu> menus = menuRepository.findByRestaurantId(restaurantId);
        if (menus.isEmpty()) {
            throw new DomainException("No menu found for restaurant: " + restaurantId);
        }
        Menu menu = menus.get(0);
        List<MenuCategory> categories = menuCategoryRepository.findByMenuId(menu.getId());
        List<MenuCategoryResponse> categoryResponses = categories.stream()
                .map(c -> {
                    List<MenuItem> items = menuItemRepository.findByMenuCategoryId(c.getId());
                    return toCategoryResponse(c, items);
                })
                .collect(Collectors.toList());
        return toMenuResponse(menu, categoryResponses);
    }

    @Override
    @Transactional
    public MenuCategoryResponse addCategory(CreateMenuCategoryRequest request, Long requesterId) {
        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new DomainException("Menu not found: " + request.getMenuId()));

        Restaurant restaurant = restaurantRepository.findById(menu.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(menu.getRestaurantId()));
        assertOwnerOrAdmin(restaurant, requesterId);

        MenuCategory category = MenuCategory.builder()
                .name(request.getName())
                .menuId(request.getMenuId())
                .build();

        MenuCategory saved = menuCategoryRepository.save(category);
        return toCategoryResponse(saved, List.of());
    }

    @Override
    @Transactional
    public MenuItemResponse addMenuItem(CreateMenuItemRequest request, Long requesterId) {
        MenuCategory category = menuCategoryRepository.findById(request.getMenuCategoryId())
                .orElseThrow(() -> new DomainException("Menu category not found: " + request.getMenuCategoryId()));

        Menu menu = menuRepository.findById(category.getMenuId())
                .orElseThrow(() -> new DomainException("Menu not found"));

        Restaurant restaurant = restaurantRepository.findById(menu.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(menu.getRestaurantId()));
        assertOwnerOrAdmin(restaurant, requesterId);

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .menuCategoryId(request.getMenuCategoryId())
                .available(true)
                .build();

        MenuItem saved = menuItemRepository.save(item);
        return toItemResponse(saved);
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long itemId, Long requesterId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new DomainException("Menu item not found: " + itemId));
        menuItemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId, Long requesterId) {
        menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new DomainException("Menu category not found: " + categoryId));
        menuCategoryRepository.deleteById(categoryId);
    }

    private void assertOwnerOrAdmin(Restaurant restaurant, Long requesterId) {
        var user = userRepository.findById(requesterId)
                .orElseThrow(() -> new DomainException("Requester not found"));
        boolean isAdmin = GlobalRole.ADMIN.equals(user.getGlobalRole());
        boolean isOwner = requesterId.equals(restaurant.getOwnerId());
        if (!isAdmin && !isOwner) {
            throw new DomainException("You do not have permission to manage this menu");
        }
    }

    private MenuResponse toMenuResponse(Menu menu, List<MenuCategoryResponse> categories) {
        return MenuResponse.builder()
                .id(menu.getId())
                .restaurantId(menu.getRestaurantId())
                .updatedAt(menu.getUpdatedAt())
                .categories(categories)
                .build();
    }

    private MenuCategoryResponse toCategoryResponse(MenuCategory c, List<MenuItem> items) {
        return MenuCategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .menuId(c.getMenuId())
                .items(items.stream().map(this::toItemResponse).collect(Collectors.toList()))
                .build();
    }

    private MenuItemResponse toItemResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .menuCategoryId(item.getMenuCategoryId())
                .available(item.isAvailable())
                .build();
    }
}
