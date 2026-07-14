package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.CreateTableRequest;
import com.restaurant.application.dto.request.UpdateTableRequest;
import com.restaurant.application.dto.response.RestaurantTableResponse;
import com.restaurant.domain.model.AuthUser;

import java.util.List;

public interface RestaurantTableUseCase {

    RestaurantTableResponse create(Long restaurantId, CreateTableRequest request, AuthUser requester);

    RestaurantTableResponse update(Long restaurantId, Long tableId, UpdateTableRequest request, AuthUser requester);

    void delete(Long restaurantId, Long tableId, AuthUser requester);

    List<RestaurantTableResponse> listByRestaurant(Long restaurantId, AuthUser requester);

    RestaurantTableResponse getById(Long restaurantId, Long tableId, AuthUser requester);
}
