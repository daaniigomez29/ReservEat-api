package com.restaurant.infrastructure.web.controller;

import com.restaurant.application.dto.request.AssignTableRequest;
import com.restaurant.application.dto.request.CreateReservationRequest;
import com.restaurant.application.dto.response.ReservationResponse;
import com.restaurant.application.port.in.ReservationUseCase;
import com.restaurant.domain.model.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationUseCase reservationUseCase;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationUseCase.createReservation(request, user.getId().value(), user.getEmail()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(reservationUseCase.getReservationById(id, user));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ReservationResponse>> myReservations(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reservationUseCase.getReservationsByUser(user.getId().value(), page, size));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReservationResponse>> byRestaurant(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(reservationUseCase.getReservationsByRestaurant(restaurantId, user));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(reservationUseCase.cancelReservation(id, user));
    }

    // --- Owner/admin operations ---

    @PatchMapping("/{id}/assign-table")
    public ResponseEntity<ReservationResponse> assignTable(
            @PathVariable Long id,
            @Valid @RequestBody AssignTableRequest request,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(
                reservationUseCase.assignTable(id, request.getTableId(), user));
    }

    @PatchMapping("/{id}/seat")
    public ResponseEntity<ReservationResponse> seat(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(reservationUseCase.seatReservation(id, user));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ReservationResponse> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(reservationUseCase.completeReservation(id, user));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<ReservationResponse> noShow(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(reservationUseCase.markNoShow(id, user));
    }
}
