package com.restaurant.infrastructure.persistence.repository;

import com.restaurant.domain.model.ReservationStatus;
import com.restaurant.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findByRestaurantId(Long restaurantId);

    List<ReservationEntity> findByBookerEmail(String bookerEmail);

    Page<ReservationEntity> findByUserId(Long userId, Pageable pageable);

    List<ReservationEntity> findByRestaurantIdAndStatus(Long restaurantId, ReservationStatus status);

    @Query("""
            SELECT r FROM ReservationEntity r
            WHERE r.restaurantId = :restaurantId
              AND r.status IN ('PENDING', 'CONFIRMED')
              AND r.startDate < :end
              AND r.endDate > :start
            """)
    List<ReservationEntity> findByRestaurantIdAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            SELECT DISTINCT r.tableId
            FROM ReservationEntity r
            WHERE r.restaurantId = :restaurantId
              AND r.tableId IS NOT NULL
              AND r.status IN ('PENDING', 'CONFIRMED', 'SEATED')
              AND r.startDate < :end
              AND r.endDate > :start
            """)
    Set<Long> findOccupiedTableIds(
            @Param("restaurantId") Long restaurantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            SELECT DISTINCT r.tableId
            FROM ReservationEntity r
            WHERE r.restaurantId = :restaurantId
              AND r.tableId IS NOT NULL
              AND r.id <> :excludeId
              AND r.status IN ('PENDING', 'CONFIRMED', 'SEATED')
              AND r.startDate < :end
              AND r.endDate > :start
            """)
    Set<Long> findOccupiedTableIdsExcluding(
            @Param("restaurantId") Long restaurantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeId") Long excludeReservationId);

    @Query("""
            SELECT r FROM ReservationEntity r
            WHERE r.restaurantId = :restaurantId
              AND r.status IN ('PENDING', 'CONFIRMED', 'SEATED')
              AND r.startDate <= :at
              AND r.endDate > :at
            """)
    List<ReservationEntity> findActiveOverlappingInstant(
            @Param("restaurantId") Long restaurantId,
            @Param("at") LocalDateTime at);

    @Query("""
            SELECT r FROM ReservationEntity r
            WHERE r.userId = :userId
              AND r.restaurantId = :restaurantId
              AND r.startDate = :startDate
              AND r.status IN ('CONFIRMED', 'SEATED')
              AND r.createdAt >= :createdAfter
            ORDER BY r.createdAt DESC
            """)
    List<ReservationEntity> findRecentDuplicates(
            @Param("userId") Long userId,
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("createdAfter") LocalDateTime createdAfter);
}
