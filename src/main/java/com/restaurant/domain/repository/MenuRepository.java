package com.restaurant.domain.repository;

import com.restaurant.domain.model.Menu;

import java.util.List;
import java.util.Optional;

public interface MenuRepository {

    Menu save(Menu menu);

    Optional<Menu> findById(Long id);

    List<Menu> findByRestaurantId(Long restaurantId);

    void deleteById(Long id);
}
