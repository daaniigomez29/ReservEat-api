package com.restaurant.infrastructure.persistence.mapper;

import com.restaurant.domain.exception.RestaurantNotFoundException;
import com.restaurant.domain.model.MenuItem;
import com.restaurant.infrastructure.persistence.entity.MenuCategoryEntity;
import com.restaurant.infrastructure.persistence.entity.MenuItemEntity;
import com.restaurant.infrastructure.persistence.repository.JpaMenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuItemMapper {
    private final JpaMenuCategoryRepository menuCategoryRepository;

    public MenuItem toDomain(MenuItemEntity e) {
        return MenuItem.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .available(e.isAvailable())
                .menuCategoryId(e.getMenuCategoryEntity().getId())
                .build();
    }

    public MenuItemEntity toEntity(MenuItem d) {
        MenuCategoryEntity menuCategoryEntity = menuCategoryRepository.findById(d.getMenuCategoryId()).orElseThrow(() -> new RestaurantNotFoundException(d.getId()));
        return MenuItemEntity.builder()
                .id(d.getId())
                .name(d.getName())
                .price(d.getPrice())
                .menuCategoryEntity(menuCategoryEntity)
                .build();
    }
}
