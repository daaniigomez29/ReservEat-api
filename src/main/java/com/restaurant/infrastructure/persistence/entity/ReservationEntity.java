package com.restaurant.infrastructure.persistence.entity;

import com.restaurant.domain.model.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations",
        indexes = {
                @Index(name = "idx_reservation_restaurant", columnList = "restaurant_id"),
                @Index(name = "idx_reservation_user", columnList = "user_id"),
                @Index(name = "idx_reservation_booker", columnList = "booker_email"),
                @Index(name = "idx_reservation_start", columnList = "start_date"),
                @Index(name = "idx_reservation_table", columnList = "table_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "party_size", nullable = false)
    private int partySize;

    @Column(name = "booker_email", nullable = false)
    private String bookerEmail;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
