package com.restaurant.infrastructure.persistence.adapter;

import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;
import com.restaurant.domain.model.Restaurant;
import com.restaurant.domain.repository.RestaurantRepository;
import com.restaurant.domain.valueobject.Location;
import com.restaurant.infrastructure.persistence.entity.AuthUserEntity;
import com.restaurant.infrastructure.persistence.entity.RestaurantEntity;
import com.restaurant.infrastructure.persistence.repository.JpaMenuRepository;
import com.restaurant.infrastructure.persistence.repository.JpaRestaurantRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RestaurantRepositoryAdapter implements RestaurantRepository {

    private final JpaRestaurantRepository jpa;
    private final JpaMenuRepository jpaMenuRepository;

    @Override
    public Restaurant save(Restaurant restaurant) {
        RestaurantEntity entity = toEntity(restaurant);
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Restaurant> findByIdUpdate(Long id) {return jpa.findByIdUpdate(id).map(this::toDomain);}

    @Override
    public List<Restaurant> findAll() {
        return jpa.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Restaurant> findByNameContaining(String name) {
        return jpa.findByNameContainingIgnoreCase(name).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Restaurant> findByCity(String city) {
        return jpa.findByCityIgnoreCase(city).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Restaurant> findByProvince(String province) {
        return jpa.findByProvinceIgnoreCase(province).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Restaurant> findByCuisineType(CuisineType cuisineType) {
        return jpa.findByCuisineType(cuisineType).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Restaurant> findByDietaryOption(DietaryOption dietaryOption) {
        return jpa.findAll().stream()
                .filter(r -> r.getDietaryOptions().contains(dietaryOption))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Restaurant> findByAveragePriceLessThanEqual(BigDecimal maxPrice) {
        return jpa.findByAveragePriceLessThanEqual(maxPrice).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Page<Restaurant> search(String name, String city, String province,
                                   CuisineType cuisineType, DietaryOption dietaryOption,
                                   BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpa.search(name, city, province, cuisineType, dietaryOption, maxPrice, pageable)
                .map(this::toDomain);
    }

    @Override
    public List<Restaurant> getOwnerRestaurants(Long ownerId) {
        return jpa.findByOwnerId(ownerId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Restaurant> findAllByIdIn(List<Long> ids) {
        return jpa.findAllById(ids).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    private RestaurantEntity toEntity(Restaurant r) {
        AuthUserEntity owner = null;
        if (r.getOwnerId() != null) {
            owner = AuthUserEntity.builder()
                    .id(r.getOwnerId())
                    .build();
        }

        return RestaurantEntity.builder()
                .id(r.getId())
                .name(r.getName())
                .email(r.getEmail())
                .tlf(r.getTlf())
                .size(r.getSize())
                .averagePrice(r.getAveragePrice())
                .cuisineType(r.getCuisineType())
                .openingTime(r.getOpeningTime())
                .closingTime(r.getClosingTime())
                .dietaryOptions(r.getDietaryOptions())
                .street(r.getLocation() != null ? r.getLocation().getStreet() : null)
                .city(r.getLocation() != null ? r.getLocation().getCity() : null)
                .province(r.getLocation() != null ? r.getLocation().getProvince() : null)
                .postalCode(r.getLocation() != null ? r.getLocation().getPostalCode() : null)
                .lat(r.getLocation() != null ? r.getLocation().getLat() : null)
                .lon(r.getLocation() != null ? r.getLocation().getLon() : null)
                .owner(owner)
                .build();
    }

    private Restaurant toDomain(RestaurantEntity e) {
        Location location = Location.builder()
                .street(e.getStreet())
                .city(e.getCity())
                .province(e.getProvince())
                .postalCode(e.getPostalCode())
                .lat(e.getLat())
                .lon(e.getLon())
                .build();

        List<Long> menuIds = jpaMenuRepository.findByRestaurantEntity_Id(e.getId())
                .stream().map(m -> m.getId()).collect(Collectors.toList());

        Long ownerId = e.getOwner() != null ? e.getOwner().getId() : null;

        return Restaurant.builder()
                .id(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .tlf(e.getTlf())
                .size(e.getSize())
                .averagePrice(e.getAveragePrice())
                .cuisineType(e.getCuisineType())
                .openingTime(e.getOpeningTime())
                .closingTime(e.getClosingTime())
                .dietaryOptions(e.getDietaryOptions())
                .location(location)
                .menuIds(menuIds)
                .ownerId(ownerId)
                .build();
    }
}
