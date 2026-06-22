package com.restaurant;

import com.restaurant.application.dto.request.CreateReservationRequest;
import com.restaurant.application.usecase.ReservationService;
import com.restaurant.domain.exception.ReservationConflictException;
import com.restaurant.domain.model.*;
import com.restaurant.domain.repository.AuthUserRepository;
import com.restaurant.domain.repository.ReservationRepository;
import com.restaurant.domain.repository.RestaurantRepository;
import com.restaurant.domain.repository.RestaurantTableRepository;
import com.restaurant.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private AuthUserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationService reservationService;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .size(20)
                .ownerId(99L)
                .build();
    }

    private RestaurantTable table(Long id, int capacity) {
        return RestaurantTable.builder()
                .id(id)
                .restaurantId(1L)
                .label("M" + id)
                .capacity(capacity)
                .shape(TableShape.RECTANGLE)
                .x(0).y(0).width(100).height(80).rotation(0)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should assign the smallest fitting free table (best-fit)")
    void createReservation_assignsSmallestFittingTable() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(13).withMinute(0);
        CreateReservationRequest request = CreateReservationRequest.builder()
                .restaurantId(1L)
                .startDate(start)
                .partySize(3)
                .build();

        when(restaurantRepository.findByIdUpdate(1L)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findOccupiedTableIds(eq(1L), any(), any()))
                .thenReturn(Set.of());
        // Tables of capacity 2, 4 and 6 are free; party of 3 must get the one of capacity 4.
        when(tableRepository.findActiveByRestaurantId(1L))
                .thenReturn(List.of(table(10L, 2), table(11L, 4), table(12L, 6)));

        when(reservationRepository.save(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        var response = reservationService.createReservation(request, 1L, "user@test.com");

        assertThat(response).isNotNull();
        assertThat(response.getPartySize()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertThat(captor.getValue().getTableId()).isEqualTo(11L); // best-fit, not the 6-seater
    }

    @Test
    @DisplayName("Should throw ReservationConflictException when no table fits in the slot")
    void createReservation_whenNoTableAvailable_shouldThrow() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(13).withMinute(0);
        CreateReservationRequest request = CreateReservationRequest.builder()
                .restaurantId(1L)
                .startDate(start)
                .partySize(6)
                .build();

        when(restaurantRepository.findByIdUpdate(1L)).thenReturn(Optional.of(restaurant));
        // The only table large enough (id 12, capacity 6) is already occupied.
        when(reservationRepository.findOccupiedTableIds(eq(1L), any(), any()))
                .thenReturn(Set.of(12L));
        when(tableRepository.findActiveByRestaurantId(1L))
                .thenReturn(List.of(table(10L, 2), table(11L, 4), table(12L, 6)));

        assertThatThrownBy(() -> reservationService.createReservation(request, 1L, "user@test.com"))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessageContaining("No table available");
    }

    @Test
    @DisplayName("Double-submit within the dedup window returns the existing reservation, no new save")
    void createReservation_whenRecentDuplicate_returnsExistingWithoutSaving() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(13).withMinute(0);
        CreateReservationRequest request = CreateReservationRequest.builder()
                .restaurantId(1L)
                .startDate(start)
                .partySize(2)
                .build();

        Reservation existing = Reservation.builder()
                .id(77L).restaurantId(1L).tableId(11L).partySize(2)
                .startDate(start).endDate(start.plusHours(2))
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(restaurantRepository.findByIdUpdate(1L)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findRecentDuplicate(eq(1L), eq(1L), eq(start), any()))
                .thenReturn(Optional.of(existing));

        var response = reservationService.createReservation(request, 1L, "user@test.com");

        assertThat(response.getId()).isEqualTo(77L);
        verify(reservationRepository, never()).save(any()); // no duplicate created
    }

    @Test
    @DisplayName("Owner can seat a CONFIRMED reservation")
    void seatReservation_whenConfirmed_setsSeated() {
        Reservation reservation = Reservation.builder()
                .id(5L).restaurantId(1L).partySize(2)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(userRepository.findById(99L)).thenReturn(Optional.of(
                AuthUser.builder().id(new UserId(99L)).globalRole(GlobalRole.USER).build()));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = reservationService.seatReservation(5L, 99L); // 99 = restaurant owner

        assertThat(response.getStatus()).isEqualTo(ReservationStatus.SEATED);
    }

    @Test
    @DisplayName("Owner can reassign a reservation to a free fitting table")
    void assignTable_whenFreeAndFits_reassigns() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(13).withMinute(0);
        Reservation reservation = Reservation.builder()
                .id(5L).restaurantId(1L).partySize(2).tableId(10L)
                .startDate(start).endDate(start.plusHours(2))
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        when(restaurantRepository.findByIdUpdate(1L)).thenReturn(Optional.of(restaurant));
        when(userRepository.findById(99L)).thenReturn(Optional.of(
                AuthUser.builder().id(new UserId(99L)).globalRole(GlobalRole.USER).build()));
        when(tableRepository.findById(11L)).thenReturn(Optional.of(table(11L, 4)));
        when(reservationRepository.findOccupiedTableIdsExcluding(eq(1L), any(), any(), eq(5L)))
                .thenReturn(Set.of());
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = reservationService.assignTable(5L, 11L, 99L);

        assertThat(response.getTableId()).isEqualTo(11L);
        assertThat(response.getTableLabel()).isEqualTo("M11");
    }
}
