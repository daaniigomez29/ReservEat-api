package com.restaurant.application.usecase;

import com.restaurant.application.dto.request.CreateReservationRequest;
import com.restaurant.application.dto.response.ReservationResponse;
import com.restaurant.application.port.in.ReservationUseCase;
import com.restaurant.domain.event.ReservationCancelledEvent;
import com.restaurant.domain.event.ReservationCreatedEvent;
import com.restaurant.domain.exception.DomainException;
import com.restaurant.domain.exception.ReservationConflictException;
import com.restaurant.domain.exception.ReservationNotFoundException;
import com.restaurant.domain.exception.RestaurantNotFoundException;
import com.restaurant.domain.exception.TableNotFoundException;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.Reservation;
import com.restaurant.domain.model.ReservationStatus;
import com.restaurant.domain.model.Restaurant;
import com.restaurant.domain.model.RestaurantTable;
import com.restaurant.domain.repository.ReservationRepository;
import com.restaurant.domain.repository.RestaurantRepository;
import com.restaurant.domain.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService implements ReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${reservation.turnover-buffer-minutes:30}")
    private int turnoverBufferMinutes;

    @Value("${reservation.dedup-window-seconds:10}")
    private int dedupWindowSeconds;

    @Override
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request,
                                                 Long userId,
                                                 String userEmail) {
        // Pessimistic lock on the restaurant serializes table assignment for this
        // restaurant, preventing two concurrent bookings from grabbing the same table.
        Restaurant restaurant = restaurantRepository.findByIdUpdate(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(request.getRestaurantId()));

        LocalDateTime start = request.getStartDate();
        LocalDateTime end = start.plusHours(Reservation.DEFAULT_DURATION_HOURS);

        if (!restaurant.isWithinOpeningHours(start.toLocalTime())) {
            throw new DomainException(String.format(
                    "The restaurant only accepts reservations between %s and %s",
                    restaurant.getOpeningTime(), restaurant.getClosingTime()));
        }

        // Idempotency: an accidental double-submit (double-click / network retry) by the
        // same user for the same slot, seen within the dedup window, returns the existing
        // reservation instead of creating a duplicate. Safe because we hold the restaurant
        // lock, so the first request has already committed before the second reaches here.
        if (userId != null) {
            LocalDateTime createdAfter = LocalDateTime.now().minusSeconds(dedupWindowSeconds);
            var duplicate = reservationRepository.findRecentDuplicate(
                    userId, restaurant.getId(), start, createdAfter);
            if (duplicate.isPresent()) {
                Reservation existing = duplicate.get();
                log.info("Duplicate reservation deduplicated: returning existing id={} for user={} restaurant={}",
                        existing.getId(), userId, restaurant.getId());
                return toResponse(existing, restaurant.getName());
            }
        }

        Long tableId = assignTableOrFail(restaurant.getId(), request.getPartySize(), start, end);

        String bookerEmail = request.getBookerEmail() != null ? request.getBookerEmail() : userEmail;

        Reservation reservation = Reservation.builder()
                .restaurantId(restaurant.getId())
                .tableId(tableId)
                .startDate(start)
                .endDate(end)
                .partySize(request.getPartySize())
                .bookerEmail(bookerEmail)
                .userId(userId)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created: id={} restaurant={} table={} partySize={}",
                saved.getId(), restaurant.getId(), tableId, saved.getPartySize());

        eventPublisher.publishEvent(new ReservationCreatedEvent(saved, restaurant.getName()));

        return toResponse(saved, restaurant.getName());
    }

    /**
     * Deferred assignment: picks the smallest free table that fits the party
     * (best-fit) to minimize fragmentation. The owner can reassign later.
     * Free = active table not occupied by an overlapping reservation, where the
     * overlap window is widened by the turnover buffer on both ends.
     */
    private Long assignTableOrFail(Long restaurantId, int partySize,
                                   LocalDateTime start, LocalDateTime end) {
        LocalDateTime windowStart = start.minusMinutes(turnoverBufferMinutes);
        LocalDateTime windowEnd = end.plusMinutes(turnoverBufferMinutes);

        Set<Long> occupied = reservationRepository.findOccupiedTableIds(
                restaurantId, windowStart, windowEnd);

        List<RestaurantTable> activeTables = tableRepository.findActiveByRestaurantId(restaurantId);

        return activeTables.stream()
                .filter(t -> !occupied.contains(t.getId()))
                .filter(t -> t.fits(partySize))
                .min(Comparator.comparingInt(RestaurantTable::getCapacity))
                .map(RestaurantTable::getId)
                .orElseThrow(() -> new ReservationConflictException(
                        String.format("No table available for %d people in the requested time slot",
                                partySize)));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, AuthUser requester) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        assertCanAccess(reservation, requester);
        Restaurant restaurant = restaurantRepository.findById(reservation.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(reservation.getRestaurantId()));
        return toResponse(reservation, restaurant.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsByUser(Long userId, int page, int size) {
        Page<Reservation> reservations = reservationRepository.findByUserId(userId, page, size);
        List<Long> restaurantIds = reservations.stream()
                .map(Reservation::getRestaurantId)
                .distinct()
                .toList();

        var restaurantsById = restaurantRepository.findAllByIdIn(restaurantIds).stream()
                .collect(Collectors.toMap(Restaurant::getId, Restaurant::getName));

        return reservations.map(r -> {
            String name = restaurantsById.getOrDefault(r.getRestaurantId(), "Unknown");
            return toResponse(r, name);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByRestaurant(Long restaurantId, AuthUser requester) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        assertRestaurantManager(restaurant, requester);

        Map<Long, String> tableLabels = tableRepository.findByRestaurantId(restaurantId).stream()
                .collect(Collectors.toMap(RestaurantTable::getId, RestaurantTable::getLabel));

        return reservationRepository.findByRestaurantId(restaurantId).stream()
                .map(r -> toOwnerResponse(r, restaurant.getName(), tableLabels.get(r.getTableId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long id, AuthUser requester) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        assertCanAccess(reservation, requester);
        reservation.cancel();
        Reservation saved = reservationRepository.save(reservation);

        Restaurant restaurant = restaurantRepository.findById(saved.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(saved.getRestaurantId()));

        eventPublisher.publishEvent(new ReservationCancelledEvent(saved, restaurant.getName()));

        return toResponse(saved, restaurant.getName());
    }

    @Override
    @Transactional
    public ReservationResponse assignTable(Long id, Long newTableId, AuthUser requester) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        // Same pessimistic lock as creation to serialize table assignment.
        Restaurant restaurant = restaurantRepository.findByIdUpdate(reservation.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(reservation.getRestaurantId()));
        assertRestaurantManager(restaurant, requester);

        RestaurantTable target = tableRepository.findById(newTableId)
                .orElseThrow(() -> new TableNotFoundException(newTableId));
        if (!restaurant.getId().equals(target.getRestaurantId())) {
            throw new DomainException("Table does not belong to this restaurant");
        }
        if (!target.fits(reservation.getPartySize())) {
            throw new DomainException("Table does not fit the party size");
        }

        LocalDateTime windowStart = reservation.getStartDate().minusMinutes(turnoverBufferMinutes);
        LocalDateTime windowEnd = reservation.getEndDate().plusMinutes(turnoverBufferMinutes);
        Set<Long> occupied = reservationRepository.findOccupiedTableIdsExcluding(
                restaurant.getId(), windowStart, windowEnd, reservation.getId());
        if (occupied.contains(newTableId)) {
            throw new ReservationConflictException("Table is already occupied in that time slot");
        }

        reservation.setTableId(newTableId);
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation {} reassigned to table {} by user {}", id, newTableId, requester.getId().value());
        return toOwnerResponse(saved, restaurant.getName(), target.getLabel());
    }

    @Override
    @Transactional
    public ReservationResponse seatReservation(Long id, AuthUser requester) {
        return applyOwnerTransition(id, requester, Reservation::seat);
    }

    @Override
    @Transactional
    public ReservationResponse completeReservation(Long id, AuthUser requester) {
        return applyOwnerTransition(id, requester, Reservation::complete);
    }

    @Override
    @Transactional
    public ReservationResponse markNoShow(Long id, AuthUser requester) {
        return applyOwnerTransition(id, requester, Reservation::markNoShow);
    }

    private ReservationResponse applyOwnerTransition(Long id, AuthUser requester,
                                                     java.util.function.Consumer<Reservation> transition) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        Restaurant restaurant = restaurantRepository.findById(reservation.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(reservation.getRestaurantId()));
        assertRestaurantManager(restaurant, requester);

        transition.accept(reservation);
        Reservation saved = reservationRepository.save(reservation);

        String tableLabel = reservation.getTableId() == null ? null
                : tableRepository.findById(reservation.getTableId())
                    .map(RestaurantTable::getLabel).orElse(null);
        return toOwnerResponse(saved, restaurant.getName(), tableLabel);
    }

    private void assertCanAccess(Reservation reservation, AuthUser requester) {
        if (!reservation.isAccessibleBy(requester)) {
            throw new DomainException("Access denied to this reservation");
        }
    }

    private void assertRestaurantManager(Restaurant restaurant, AuthUser requester) {
        if (!restaurant.isOwnerOrAdmin(requester)) {
            throw new DomainException("Only the restaurant owner or admin can perform this action");
        }
    }

    private ReservationResponse toResponse(Reservation r, String restaurantName) {
        return ReservationResponse.builder()
                .id(r.getId())
                .restaurantId(r.getRestaurantId())
                .restaurantName(restaurantName)
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .partySize(r.getPartySize())
                .bookerEmail(r.getBookerEmail())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }

    /** Owner-facing view: also exposes the assigned table. */
    private ReservationResponse toOwnerResponse(Reservation r, String restaurantName, String tableLabel) {
        return ReservationResponse.builder()
                .id(r.getId())
                .restaurantId(r.getRestaurantId())
                .restaurantName(restaurantName)
                .tableId(r.getTableId())
                .tableLabel(tableLabel)
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .partySize(r.getPartySize())
                .bookerEmail(r.getBookerEmail())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
