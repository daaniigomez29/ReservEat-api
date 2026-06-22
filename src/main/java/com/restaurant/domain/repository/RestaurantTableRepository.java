package com.restaurant.domain.repository;

import com.restaurant.domain.model.RestaurantTable;

import java.util.List;
import java.util.Optional;

public interface RestaurantTableRepository {

    RestaurantTable save(RestaurantTable table);

    Optional<RestaurantTable> findById(Long id);

    List<RestaurantTable> findByRestaurantId(Long restaurantId);

    List<RestaurantTable> findActiveByRestaurantId(Long restaurantId);

    boolean existsByRestaurantIdAndLabel(Long restaurantId, String label);

    boolean existsByRestaurantIdAndLabelAndIdNot(Long restaurantId, String label, Long excludeId);

    void deleteById(Long id);
}
