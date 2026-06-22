package com.restaurant;

import com.restaurant.application.usecase.ReservationReminderService;
import com.restaurant.domain.event.ReservationReminderEvent;
import com.restaurant.domain.model.Reservation;
import com.restaurant.domain.model.ReservationStatus;
import com.restaurant.domain.model.Restaurant;
import com.restaurant.domain.repository.ReservationRepository;
import com.restaurant.domain.repository.RestaurantRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationReminderServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ReservationReminderService reminderService;

    private Reservation due(Long id, Long restaurantId) {
        return Reservation.builder()
                .id(id).restaurantId(restaurantId).partySize(2)
                .startDate(LocalDateTime.now().plusHours(20))
                .endDate(LocalDateTime.now().plusHours(22))
                .bookerEmail("booker@test.com")
                .status(ReservationStatus.CONFIRMED)
                .reminderSent(false)
                .build();
    }

    @Test
    @DisplayName("Marks each due reservation as reminded and publishes one reminder event per booking")
    void sendDueReminders_marksAndPublishes() {
        ReflectionTestUtils.setField(reminderService, "leadTimeHours", 24L);

        Reservation r1 = due(1L, 10L);
        Reservation r2 = due(2L, 10L);
        when(reservationRepository.findRemindable(any(), any())).thenReturn(List.of(r1, r2));
        when(restaurantRepository.findAllByIdIn(any()))
                .thenReturn(List.of(Restaurant.builder().id(10L).name("Resto").build()));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int sent = reminderService.sendDueReminders();

        assertThat(sent).isEqualTo(2);
        assertThat(r1.isReminderSent()).isTrue();
        assertThat(r2.isReminderSent()).isTrue();

        ArgumentCaptor<ReservationReminderEvent> captor =
                ArgumentCaptor.forClass(ReservationReminderEvent.class);
        verify(eventPublisher, org.mockito.Mockito.times(2)).publishEvent(captor.capture());
        assertThat(captor.getAllValues()).allMatch(e -> e.restaurantName().equals("Resto"));
    }

    @Test
    @DisplayName("Does nothing when there are no due reservations")
    void sendDueReminders_whenNoneDue_noop() {
        when(reservationRepository.findRemindable(any(), any())).thenReturn(List.of());

        int sent = reminderService.sendDueReminders();

        assertThat(sent).isZero();
        verify(reservationRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(ReservationReminderEvent.class));
    }
}
