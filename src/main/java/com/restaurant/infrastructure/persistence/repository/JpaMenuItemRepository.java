package com.restaurant.infrastructure.persistence.repository;

import com.restaurant.infrastructure.persistence.entity.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaMenuItemRepository extends JpaRepository<MenuItemEntity, Long> {
    List<MenuItemEntity> findByMenuCategoryEntity_Id(Long menuCategoryId);
}
