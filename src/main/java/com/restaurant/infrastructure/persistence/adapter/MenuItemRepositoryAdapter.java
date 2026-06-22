package com.restaurant.infrastructure.persistence.adapter;

import com.restaurant.domain.model.MenuItem;
import com.restaurant.domain.repository.MenuItemRepository;
import com.restaurant.infrastructure.persistence.mapper.MenuItemMapper;
import com.restaurant.infrastructure.persistence.repository.JpaMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MenuItemRepositoryAdapter implements MenuItemRepository {

    private final JpaMenuItemRepository jpa;
    private final MenuItemMapper menuItemMapper;

    @Override
    public MenuItem save(MenuItem item) {
        return menuItemMapper.toDomain(jpa.save(menuItemMapper.toEntity(item)));
    }

    @Override
    public Optional<MenuItem> findById(Long id) {
        return jpa.findById(id).map(menuItemMapper::toDomain);
    }

    @Override
    public List<MenuItem> findByMenuCategoryId(Long menuCategoryId) {
        return jpa.findByMenuCategoryEntity_Id(menuCategoryId).stream().map(menuItemMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
