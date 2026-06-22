package com.restaurant.infrastructure.persistence.repository;

import com.restaurant.infrastructure.persistence.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaMenuRepository extends JpaRepository<MenuEntity, Long> {
    List<MenuEntity> findByRestaurantEntity_Id(Long restaurantId);
}
