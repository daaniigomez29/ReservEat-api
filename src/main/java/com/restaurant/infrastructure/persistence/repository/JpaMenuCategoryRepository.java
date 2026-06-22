package com.restaurant.infrastructure.persistence.repository;

import com.restaurant.infrastructure.persistence.entity.MenuCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaMenuCategoryRepository extends JpaRepository<MenuCategoryEntity, Long> {
    List<MenuCategoryEntity> findByMenuEntity_Id(Long menuId);
}
