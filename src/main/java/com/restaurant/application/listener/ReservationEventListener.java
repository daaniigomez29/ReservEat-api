package com.restaurant.application.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.restaurant.application.port.out.EmailNotificationPort;
import com.restaurant.domain.event.ReservationCancelledEvent;
import com.restaurant.domain.event.ReservationCreatedEvent;
import com.restaurant.domain.event.ReservationReminderEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationEventListener {
    private final EmailNotificationPort emailNotificationPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationCreated(ReservationCreatedEvent event) {
        emailNotificationPort.sendReservationConfirmation(event.reservation(), event.restaurantName());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationCancelled(ReservationCancelledEvent event) {
        emailNotificationPort.sendReservationCancellation(event.reservation(), event.restaurantName());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationReminder(ReservationReminderEvent event) {
        emailNotificationPort.sendReservationReminder(event.reservation(), event.restaurantName());
    }
}
