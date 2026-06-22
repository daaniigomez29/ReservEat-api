package com.restaurant.infrastructure.persistence.mapper;

import com.restaurant.domain.exception.RestaurantNotFoundException;
import com.restaurant.domain.model.Menu;
import com.restaurant.domain.model.MenuCategory;
import com.restaurant.infrastructure.persistence.entity.MenuCategoryEntity;
import com.restaurant.infrastructure.persistence.entity.MenuEntity;
import com.restaurant.infrastructure.persistence.entity.RestaurantEntity;
import com.restaurant.infrastructure.persistence.repository.JpaMenuCategoryRepository;
import com.restaurant.infrastructure.persistence.repository.JpaRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MenuMapper {
    private MenuCategoryMapper menuCategoryMapper;
    private JpaMenuCategoryRepository menuCategoryRepository;
    private JpaRestaurantRepository restaurantRepository;

    public Menu toDomain(MenuEntity c) {
        List<MenuCategory> domainList = menuCategoryRepository.findByMenuEntity_Id(c.getId()).stream().map(menuCategoryMapper::toDomain).toList();
        return Menu.builder()
                .id(c.getId())
                .menuCategories(domainList)
                .restaurantId(c.getRestaurantEntity().getId())
                .build();
    }

    public MenuEntity toEntity(Menu c) {
        List<MenuCategoryEntity> entityList = menuCategoryRepository.findByMenuEntity_Id(c.getId());
        RestaurantEntity restaurantEntity = restaurantRepository.findById(c.getRestaurantId()).orElseThrow(() -> new RestaurantNotFoundException(c.getRestaurantId()));
        return MenuEntity.builder()
                .id(c.getId())
                .menuCategoryEntities(entityList)
                .restaurantEntity(restaurantEntity)
                .build();
    }
}
