package com.restaurant.infrastructure.persistence.adapter;

import com.restaurant.domain.model.Reservation;
import com.restaurant.domain.model.ReservationStatus;
import com.restaurant.domain.repository.ReservationRepository;
import com.restaurant.infrastructure.persistence.entity.ReservationEntity;
import com.restaurant.infrastructure.persistence.repository.JpaReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationRepository jpa;

    @Override
    public Reservation save(Reservation reservation) {
        return toDomain(jpa.save(toEntity(reservation)));
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Reservation> findByRestaurantId(Long restaurantId) {
        return jpa.findByRestaurantId(restaurantId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByBookerEmail(String bookerEmail) {
        return jpa.findByBookerEmail(bookerEmail).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Page<Reservation> findByUserId(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return jpa.findByUserId(userId, pageable).map(this::toDomain);
    }

    @Override
    public List<Reservation> findByRestaurantIdAndDateRange(Long restaurantId, LocalDateTime start, LocalDateTime end) {
        return jpa.findByRestaurantIdAndDateRange(restaurantId, start, end).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Set<Long> findOccupiedTableIds(Long restaurantId, LocalDateTime start, LocalDateTime end) {
        return jpa.findOccupiedTableIds(restaurantId, start, end);
    }

    @Override
    public Set<Long> findOccupiedTableIdsExcluding(Long restaurantId, LocalDateTime start,
                                                   LocalDateTime end, Long excludeReservationId) {
        return jpa.findOccupiedTableIdsExcluding(restaurantId, start, end, excludeReservationId);
    }

    @Override
    public List<Reservation> findActiveOverlappingInstant(Long restaurantId, LocalDateTime at) {
        return jpa.findActiveOverlappingInstant(restaurantId, at).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Reservation> findRecentDuplicate(Long userId, Long restaurantId,
                                                     LocalDateTime startDate, LocalDateTime createdAfter) {
        return jpa.findRecentDuplicates(userId, restaurantId, startDate, createdAfter).stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public List<Reservation> findByRestaurantIdAndStatus(Long restaurantId, ReservationStatus status) {
        return jpa.findByRestaurantIdAndStatus(restaurantId, status).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    private ReservationEntity toEntity(Reservation r) {
        return ReservationEntity.builder()
                .id(r.getId())
                .restaurantId(r.getRestaurantId())
                .tableId(r.getTableId())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .partySize(r.getPartySize())
                .bookerEmail(r.getBookerEmail())
                .userId(r.getUserId())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private Reservation toDomain(ReservationEntity e) {
        return Reservation.builder()
                .id(e.getId())
                .restaurantId(e.getRestaurantId())
                .tableId(e.getTableId())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .partySize(e.getPartySize())
                .bookerEmail(e.getBookerEmail())
                .userId(e.getUserId())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
