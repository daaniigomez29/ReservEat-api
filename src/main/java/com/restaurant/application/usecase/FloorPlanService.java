package com.restaurant.application.usecase;

import com.restaurant.application.dto.response.FloorPlanResponse;
import com.restaurant.application.dto.response.TableStateResponse;
import com.restaurant.application.dto.response.TableStatus;
import com.restaurant.application.port.in.FloorPlanUseCase;
import com.restaurant.domain.exception.DomainException;
import com.restaurant.domain.exception.RestaurantNotFoundException;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.Reservation;
import com.restaurant.domain.model.ReservationStatus;
import com.restaurant.domain.model.Restaurant;
import com.restaurant.domain.model.RestaurantTable;
import com.restaurant.domain.repository.ReservationRepository;
import com.restaurant.domain.repository.RestaurantRepository;
import com.restaurant.domain.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FloorPlanService implements FloorPlanUseCase {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional(readOnly = true)
    public FloorPlanResponse getFloorPlan(Long restaurantId, LocalDateTime at, AuthUser requester) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
                
        assertRestaurantManager(restaurant, requester);

        LocalDateTime instant = at != null ? at : LocalDateTime.now();

        List<RestaurantTable> tables = tableRepository.findActiveByRestaurantId(restaurantId);

        // One reservation per table at a given instant (tables are not shared concurrently).
        Map<Long, Reservation> reservationByTable = reservationRepository
                .findActiveOverlappingInstant(restaurantId, instant).stream()
                .filter(r -> r.getTableId() != null)
                .collect(Collectors.toMap(
                        Reservation::getTableId,
                        Function.identity(),
                        (a, b) -> a));

        List<TableStateResponse> tableStates = tables.stream()
                .map(table -> toTableState(table, reservationByTable.get(table.getId())))
                .collect(Collectors.toList());

        return FloorPlanResponse.builder()
                .restaurantId(restaurantId)
                .queriedAt(instant)
                .planWidth(RestaurantTable.PLAN_MAX_WIDTH)
                .planHeight(RestaurantTable.PLAN_MAX_HEIGHT)
                .tables(tableStates)
                .build();
    }

    private TableStateResponse toTableState(RestaurantTable table, Reservation reservation) {
        TableStateResponse.TableStateResponseBuilder builder = TableStateResponse.builder()
                .tableId(table.getId())
                .label(table.getLabel())
                .capacity(table.getCapacity())
                .minCapacity(table.getMinCapacity())
                .zone(table.getZone())
                .shape(table.getShape())
                .x(table.getX())
                .y(table.getY())
                .width(table.getWidth())
                .height(table.getHeight())
                .rotation(table.getRotation());

        if (reservation == null) {
            return builder.status(TableStatus.FREE).build();
        }

        return builder
                .status(mapStatus(reservation.getStatus()))
                .reservationId(reservation.getId())
                .partySize(reservation.getPartySize())
                .bookerEmail(reservation.getBookerEmail())
                .occupiedUntil(reservation.getEndDate())
                .build();
    }

    private TableStatus mapStatus(ReservationStatus status) {
        return switch (status) {
            case PENDING -> TableStatus.PENDING;
            case CONFIRMED -> TableStatus.RESERVED;
            case SEATED -> TableStatus.SEATED;
            // Terminal/non-occupying states are never returned by the occupancy query.
            default -> TableStatus.RESERVED;
        };
    }

    private void assertRestaurantManager(Restaurant restaurant, AuthUser requester) {
        if (!restaurant.isOwnerOrAdmin(requester)) {
            throw new DomainException("Only the restaurant owner or admin can view the floor plan");
        }
    }
}
