package com.restaurant.infrastructure.persistence.adapter;

import com.restaurant.domain.model.Menu;
import com.restaurant.domain.repository.MenuRepository;
import com.restaurant.infrastructure.persistence.mapper.MenuMapper;
import com.restaurant.infrastructure.persistence.repository.JpaMenuCategoryRepository;
import com.restaurant.infrastructure.persistence.repository.JpaMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MenuRepositoryAdapter implements MenuRepository {

    private final JpaMenuRepository jpa;
    private final JpaMenuCategoryRepository jpaCategoryRepository;
    private final MenuMapper menuMapper;

    @Override
    public Menu save(Menu menu) {
        return menuMapper.toDomain(jpa.save(menuMapper.toEntity(menu)));
    }

    @Override
    public Optional<Menu> findById(Long id) {
        return jpa.findById(id).map(menuMapper::toDomain);
    }

    @Override
    public List<Menu> findByRestaurantId(Long restaurantId) {
        return jpa.findByRestaurantEntity_Id(restaurantId).stream().map(menuMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
