package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.CreateRestaurantRequest;
import com.restaurant.application.dto.response.RestaurantResponse;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;

public interface RestaurantUseCase {

    RestaurantResponse createRestaurant(CreateRestaurantRequest request, Long ownerId);

    RestaurantResponse getRestaurantById(Long id);

    List<RestaurantResponse> getAllRestaurants();

    Page<RestaurantResponse> searchRestaurants(String name, String city, String province,
                                               CuisineType cuisineType,
                                               DietaryOption dietaryOption,
                                               BigDecimal maxPrice, int page, int size);
                                               
    List<RestaurantResponse> getOwnerRestaurants(Long ownerId);

    RestaurantResponse updateRestaurant(Long id, CreateRestaurantRequest request, AuthUser requester);

    void deleteRestaurant(Long id, AuthUser requester);
}
