package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.CreateTableRequest;
import com.restaurant.application.dto.request.UpdateTableRequest;
import com.restaurant.application.dto.response.RestaurantTableResponse;

import java.util.List;

public interface RestaurantTableUseCase {

    RestaurantTableResponse create(Long restaurantId, CreateTableRequest request, Long requesterId);

    RestaurantTableResponse update(Long restaurantId, Long tableId, UpdateTableRequest request, Long requesterId);

    void delete(Long restaurantId, Long tableId, Long requesterId);

    List<RestaurantTableResponse> listByRestaurant(Long restaurantId, Long requesterId);

    RestaurantTableResponse getById(Long restaurantId, Long tableId, Long requesterId);
}
