package com.restaurant.application.port.in;

import com.restaurant.application.dto.request.CreateReservationRequest;
import com.restaurant.application.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReservationUseCase {

    ReservationResponse createReservation(CreateReservationRequest request, Long userId, String userEmail);

    ReservationResponse getReservationById(Long id, Long requesterId);

    Page<ReservationResponse> getReservationsByUser(Long userId, int page, int size);

    List<ReservationResponse> getReservationsByRestaurant(Long restaurantId, Long requesterId);

    ReservationResponse cancelReservation(Long id, Long requesterId);

    // Owner/admin operations
    ReservationResponse assignTable(Long id, Long tableId, Long requesterId);

    ReservationResponse seatReservation(Long id, Long requesterId);

    ReservationResponse completeReservation(Long id, Long requesterId);

    ReservationResponse markNoShow(Long id, Long requesterId);
}
