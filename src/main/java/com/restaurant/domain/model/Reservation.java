package com.restaurant.domain.model;

import com.restaurant.domain.exception.DomainException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    private Long id;
    private Long restaurantId;
    private Long tableId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int partySize;
    private String bookerEmail;
    private Long userId;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    // Whether the upcoming-reservation reminder email has already been sent.
    private boolean reminderSent;

    // Default reservation window is 2 hours
    public static final int DEFAULT_DURATION_HOURS = 2;

    public boolean isActive() {
        return ReservationStatus.CONFIRMED.equals(this.status)
                || ReservationStatus.SEATED.equals(this.status);
    }

    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return this.startDate.isBefore(otherEnd) && this.endDate.isAfter(otherStart);
    }

    public void cancel() {
        if (ReservationStatus.COMPLETED.equals(this.status)
                || ReservationStatus.NO_SHOW.equals(this.status)) {
            throw new DomainException("Cannot cancel a finished reservation");
        }
        this.status = ReservationStatus.CANCELLED;
    }

    /** Guests have arrived and been seated at their table. */
    public void seat() {
        if (!ReservationStatus.CONFIRMED.equals(this.status)) {
            throw new DomainException("Only a CONFIRMED reservation can be seated");
        }
        this.status = ReservationStatus.SEATED;
    }

    /** Service finished; the table is released. */
    public void complete() {
        if (!ReservationStatus.SEATED.equals(this.status)
                && !ReservationStatus.CONFIRMED.equals(this.status)) {
            throw new DomainException("Only a SEATED or CONFIRMED reservation can be completed");
        }
        this.status = ReservationStatus.COMPLETED;
    }

    public void markReminderSent() {
        this.reminderSent = true;
    }

    /** The party never showed up. */
    public void markNoShow() {
        if (!ReservationStatus.CONFIRMED.equals(this.status)) {
            throw new DomainException("Only a CONFIRMED reservation can be marked as no-show");
        }
        this.status = ReservationStatus.NO_SHOW;
    }
}
