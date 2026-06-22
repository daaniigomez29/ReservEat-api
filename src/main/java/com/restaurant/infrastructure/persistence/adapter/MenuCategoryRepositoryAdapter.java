package com.restaurant.infrastructure.persistence.adapter;

import com.restaurant.domain.model.MenuCategory;
import com.restaurant.domain.repository.MenuCategoryRepository;
import com.restaurant.infrastructure.persistence.entity.MenuCategoryEntity;
import com.restaurant.infrastructure.persistence.mapper.MenuCategoryMapper;
import com.restaurant.infrastructure.persistence.repository.JpaMenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MenuCategoryRepositoryAdapter implements MenuCategoryRepository {

    private final JpaMenuCategoryRepository jpa;
    private final MenuCategoryMapper menuCategoryMapper;

    @Override
    public MenuCategory save(MenuCategory category) {
        return menuCategoryMapper.toDomain(jpa.save(toEntity(category)));
    }

    @Override
    public Optional<MenuCategory> findById(Long id) {
        return jpa.findById(id).map(menuCategoryMapper::toDomain);
    }

    @Override
    public List<MenuCategory> findByMenuId(Long menuId) {
        return jpa.findByMenuEntity_Id(menuId).stream().map(menuCategoryMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    private MenuCategoryEntity toEntity(MenuCategory c) {
        return MenuCategoryEntity.builder()
                .id(c.getId())
                .name(c.getName())
                .build();
    }
}
