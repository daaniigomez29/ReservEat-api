package com.restaurant.domain.repository;

import com.restaurant.domain.model.MenuItem;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository {

    MenuItem save(MenuItem item);

    Optional<MenuItem> findById(Long id);

    List<MenuItem> findByMenuCategoryId(Long menuCategoryId);

    void deleteById(Long id);
}
