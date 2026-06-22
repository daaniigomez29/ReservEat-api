package com.restaurant.infrastructure.persistence.repository;

import com.restaurant.infrastructure.persistence.entity.RestaurantTableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRestaurantTableRepository extends JpaRepository<RestaurantTableEntity, Long> {

    List<RestaurantTableEntity> findByRestaurantId(Long restaurantId);

    List<RestaurantTableEntity> findByRestaurantIdAndActiveTrue(Long restaurantId);

    boolean existsByRestaurantIdAndLabel(Long restaurantId, String label);

    boolean existsByRestaurantIdAndLabelAndIdNot(Long restaurantId, String label, Long id);
}
