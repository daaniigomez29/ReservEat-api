package com.restaurant.infrastructure.scheduler;

import com.restaurant.application.port.in.ReservationReminderUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically triggers the upcoming-reservation reminder job. The {@code reminderSent}
 * flag makes each run idempotent, so a missed tick (downtime) is recovered on the next.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationReminderScheduler {

    private final ReservationReminderUseCase reservationReminderUseCase;

    @Scheduled(cron = "${reservation.reminder.cron:0 */15 * * * *}")
    public void run() {
        try {
            reservationReminderUseCase.sendDueReminders();
        } catch (Exception e) {
            // Never let a failed run kill the scheduler thread; just log and retry next tick.
            log.error("Reservation reminder job failed: {}", e.getMessage(), e);
        }
    }
}
