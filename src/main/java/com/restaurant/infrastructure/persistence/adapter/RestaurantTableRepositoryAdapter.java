package com.restaurant.infrastructure.persistence.adapter;

import com.restaurant.domain.model.RestaurantTable;
import com.restaurant.domain.repository.RestaurantTableRepository;
import com.restaurant.infrastructure.persistence.entity.RestaurantTableEntity;
import com.restaurant.infrastructure.persistence.repository.JpaRestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RestaurantTableRepositoryAdapter implements RestaurantTableRepository {

    private final JpaRestaurantTableRepository jpa;

    @Override
    public RestaurantTable save(RestaurantTable table) {
        return toDomain(jpa.save(toEntity(table)));
    }

    @Override
    public Optional<RestaurantTable> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<RestaurantTable> findByRestaurantId(Long restaurantId) {
        return jpa.findByRestaurantId(restaurantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantTable> findActiveByRestaurantId(Long restaurantId) {
        return jpa.findByRestaurantIdAndActiveTrue(restaurantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByRestaurantIdAndLabel(Long restaurantId, String label) {
        return jpa.existsByRestaurantIdAndLabel(restaurantId, label);
    }

    @Override
    public boolean existsByRestaurantIdAndLabelAndIdNot(Long restaurantId, String label, Long excludeId) {
        return jpa.existsByRestaurantIdAndLabelAndIdNot(restaurantId, label, excludeId);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    private RestaurantTableEntity toEntity(RestaurantTable t) {
        return RestaurantTableEntity.builder()
                .id(t.getId())
                .restaurantId(t.getRestaurantId())
                .label(t.getLabel())
                .capacity(t.getCapacity())
                .minCapacity(t.getMinCapacity())
                .zone(t.getZone())
                .shape(t.getShape())
                .x(t.getX())
                .y(t.getY())
                .width(t.getWidth())
                .height(t.getHeight())
                .rotation(t.getRotation())
                .active(t.isActive())
                .build();
    }

    private RestaurantTable toDomain(RestaurantTableEntity e) {
        return RestaurantTable.builder()
                .id(e.getId())
                .restaurantId(e.getRestaurantId())
                .label(e.getLabel())
                .capacity(e.getCapacity())
                .minCapacity(e.getMinCapacity())
                .zone(e.getZone())
                .shape(e.getShape())
                .x(e.getX())
                .y(e.getY())
                .width(e.getWidth())
                .height(e.getHeight())
                .rotation(e.getRotation())
                .active(e.isActive())
                .build();
    }
}
