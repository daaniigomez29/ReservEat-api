package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.CreateRestaurantRequest;
import com.restaurant.application.dto.response.RestaurantResponse;
import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;

import java.math.BigDecimal;
import java.util.List;

public interface RestaurantUseCase {

    RestaurantResponse createRestaurant(CreateRestaurantRequest request, Long ownerId);

    RestaurantResponse getRestaurantById(Long id);

    List<RestaurantResponse> getAllRestaurants();

    List<RestaurantResponse> searchRestaurants(String name, String city, String province,
                                               CuisineType cuisineType,
                                               DietaryOption dietaryOption,
                                               BigDecimal maxPrice);

    RestaurantResponse updateRestaurant(Long id, CreateRestaurantRequest request, Long requesterId);

    void deleteRestaurant(Long id, Long requesterId);
}
