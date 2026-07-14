package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.CreateReservationRequest;
import com.restaurant.application.dto.response.ReservationResponse;
import com.restaurant.domain.model.AuthUser;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReservationUseCase {

    ReservationResponse createReservation(CreateReservationRequest request, Long userId, String userEmail);

    ReservationResponse getReservationById(Long id, AuthUser requester);

    Page<ReservationResponse> getReservationsByUser(Long userId, int page, int size);

    List<ReservationResponse> getReservationsByRestaurant(Long restaurantId, AuthUser requester);

    ReservationResponse cancelReservation(Long id, AuthUser requester);

    // Owner/admin operations
    ReservationResponse assignTable(Long id, Long tableId, AuthUser requester);

    ReservationResponse seatReservation(Long id, AuthUser requester);

    ReservationResponse completeReservation(Long id, AuthUser requester);

    ReservationResponse markNoShow(Long id, AuthUser requester);
}
