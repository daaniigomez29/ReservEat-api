package com.restaurant.domain.repository;

import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;
import com.restaurant.domain.model.Restaurant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface RestaurantRepository {

    Restaurant save(Restaurant restaurant);

    Optional<Restaurant> findById(Long id);

    Optional<Restaurant> findByIdUpdate(Long id);

    List<Restaurant> findAll();

    List<Restaurant> findByNameContaining(String name);

    List<Restaurant> findByCity(String city);

    List<Restaurant> findByProvince(String province);

    List<Restaurant> findByCuisineType(CuisineType cuisineType);

    List<Restaurant> findByDietaryOption(DietaryOption dietaryOption);

    List<Restaurant> findByAveragePriceLessThanEqual(BigDecimal maxPrice);

    Page<Restaurant> search(String name, String city, String province,
                            CuisineType cuisineType, DietaryOption dietaryOption,
                            BigDecimal maxPrice, int page, int size);

    List<Restaurant> findAllByIdIn(List<Long> ids);

    List<Restaurant> getOwnerRestaurants(Long ownerId);

    boolean existsByEmail(String email);

    void deleteById(Long id);
}
