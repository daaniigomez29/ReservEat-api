package com.restaurant.domain.repository;

import com.restaurant.domain.model.MenuCategory;

import java.util.List;
import java.util.Optional;

public interface MenuCategoryRepository {

    MenuCategory save(MenuCategory category);

    Optional<MenuCategory> findById(Long id);

    List<MenuCategory> findByMenuId(Long menuId);

    void deleteById(Long id);
}
