package com.restaurant.infrastructure.persistence.mapper;

import com.restaurant.domain.exception.RestaurantNotFoundException;
import com.restaurant.domain.model.MenuCategory;
import com.restaurant.domain.model.MenuItem;
import com.restaurant.infrastructure.persistence.entity.MenuCategoryEntity;
import com.restaurant.infrastructure.persistence.entity.MenuEntity;
import com.restaurant.infrastructure.persistence.entity.MenuItemEntity;
import com.restaurant.infrastructure.persistence.repository.JpaMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MenuCategoryMapper {
    private final MenuItemMapper menuItemMapper;
    private final JpaMenuRepository menuRepository;

    public MenuCategory toDomain(MenuCategoryEntity e) {
        List<MenuItem> items = e.getMenuItemEntities()
                .stream()
                .map(menuItemMapper::toDomain)
                .toList();

        return MenuCategory.builder()
                .id(e.getId())
                .name(e.getName())
                .menuId(e.getMenuEntity().getId())
                .menuItems(items)
                .build();
    }

    public MenuCategoryEntity toEntity(MenuCategory e) {
        List<MenuItemEntity> menuItemEntities = e.getMenuItems().stream().map(menuItemMapper::toEntity).toList();
        MenuEntity menuEntity = menuRepository.findById(e.getMenuId()).orElseThrow(() -> new RestaurantNotFoundException(e.getMenuId()));
        return MenuCategoryEntity.builder()
                .id(e.getId())
                .name(e.getName())
                .menuItemEntities(menuItemEntities)
                .menuEntity(menuEntity)
                .build();
    }
}
