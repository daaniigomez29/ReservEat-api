package com.restaurant.application.usecase;

import com.restaurant.application.port.in.ReservationReminderUseCase;
import com.restaurant.domain.event.ReservationReminderEvent;
import com.restaurant.domain.model.Reservation;
import com.restaurant.domain.model.Restaurant;
import com.restaurant.domain.repository.ReservationRepository;
import com.restaurant.domain.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationReminderService implements ReservationReminderUseCase {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${reservation.reminder.lead-time-hours:24}")
    private long leadTimeHours;

    @Override
    @Transactional
    public int sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime until = now.plusHours(leadTimeHours);

        List<Reservation> due = reservationRepository.findRemindable(now, until);
        if (due.isEmpty()) {
            return 0;
        }

        List<Long> restaurantIds = due.stream()
                .map(Reservation::getRestaurantId)
                .distinct()
                .toList();
        Map<Long, String> namesById = restaurantRepository.findAllByIdIn(restaurantIds).stream()
                .collect(Collectors.toMap(Restaurant::getId, Restaurant::getName));

        for (Reservation reservation : due) {
            // Flag is persisted inside this transaction; the email is only sent once the
            // transaction commits (AFTER_COMMIT listener), so a failure here means no email.
            reservation.markReminderSent();
            reservationRepository.save(reservation);

            String restaurantName = namesById.getOrDefault(reservation.getRestaurantId(), "tu restaurante");
            eventPublisher.publishEvent(new ReservationReminderEvent(reservation, restaurantName));
        }

        log.info("Dispatched {} reservation reminder(s)", due.size());
        return due.size();
    }
}
