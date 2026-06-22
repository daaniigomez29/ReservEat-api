package com.restaurant.domain.repository;

import com.restaurant.domain.model.Reservation;
import com.restaurant.domain.model.ReservationStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByRestaurantId(Long restaurantId);

    List<Reservation> findByBookerEmail(String bookerEmail);

    Page<Reservation> findByUserId(Long userId, int page, int size);

    List<Reservation> findByRestaurantIdAndDateRange(Long restaurantId,
                                                     LocalDateTime start,
                                                     LocalDateTime end);

    /**
     * Table ids occupied by active reservations overlapping the [start, end) window.
     * Used to compute which tables are free when assigning a new reservation.
     */
    Set<Long> findOccupiedTableIds(Long restaurantId,
                                   LocalDateTime start,
                                   LocalDateTime end);

    /**
     * Same as {@link #findOccupiedTableIds} but ignores a given reservation,
     * used when reassigning a table so the reservation does not conflict with itself.
     */
    Set<Long> findOccupiedTableIdsExcluding(Long restaurantId,
                                            LocalDateTime start,
                                            LocalDateTime end,
                                            Long excludeReservationId);

    List<Reservation> findActiveOverlappingInstant(Long restaurantId, LocalDateTime at);

    /**
     * Most recent active reservation by the same user for the same restaurant and
     * slot, created at/after {@code createdAfter}. Used to deduplicate accidental
     * double-submits (double-click / network retry).
     */
    Optional<Reservation> findRecentDuplicate(Long userId,
                                              Long restaurantId,
                                              LocalDateTime startDate,
                                              LocalDateTime createdAfter);

    List<Reservation> findByRestaurantIdAndStatus(Long restaurantId, ReservationStatus status);

    /**
     * Confirmed reservations starting within [from, to] that have not yet had their
     * upcoming-reservation reminder sent. Used by the scheduled reminder job.
     */
    List<Reservation> findRemindable(LocalDateTime from, LocalDateTime to);

    void deleteById(Long id);
}
