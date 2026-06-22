package com.restaurant.application.usecase;

import com.restaurant.application.dto.request.CreateTableRequest;
import com.restaurant.application.dto.request.UpdateTableRequest;
import com.restaurant.application.dto.response.RestaurantTableResponse;
import com.restaurant.application.port.in.RestaurantTableUseCase;
import com.restaurant.domain.exception.DomainException;
import com.restaurant.domain.exception.RestaurantNotFoundException;
import com.restaurant.domain.exception.TableNotFoundException;
import com.restaurant.domain.model.GlobalRole;
import com.restaurant.domain.model.Restaurant;
import com.restaurant.domain.model.RestaurantTable;
import com.restaurant.domain.repository.AuthUserRepository;
import com.restaurant.domain.repository.RestaurantRepository;
import com.restaurant.domain.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantTableService implements RestaurantTableUseCase {

    private final RestaurantTableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuthUserRepository userRepository;

    @Override
    @Transactional
    public RestaurantTableResponse create(Long restaurantId, CreateTableRequest request, Long requesterId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        assertRestaurantManager(restaurant, requesterId);

        if (tableRepository.existsByRestaurantIdAndLabel(restaurantId, request.getLabel())) {
            throw new DomainException("A table with label '" + request.getLabel() + "' already exists");
        }

        RestaurantTable table = RestaurantTable.builder()
                .restaurantId(restaurantId)
                .label(request.getLabel())
                .capacity(request.getCapacity())
                .minCapacity(request.getMinCapacity())
                .zone(request.getZone())
                .shape(request.getShape())
                .x(request.getX())
                .y(request.getY())
                .width(request.getWidth())
                .height(request.getHeight())
                .rotation(request.getRotation())
                .active(true)
                .build();
        table.validateInvariants();

        RestaurantTable saved = tableRepository.save(table);
        log.info("Table created: id={} restaurant={} label={}", saved.getId(), restaurantId, saved.getLabel());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public RestaurantTableResponse update(Long restaurantId, Long tableId, UpdateTableRequest request, Long requesterId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        assertRestaurantManager(restaurant, requesterId);

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException(tableId));
        assertTableBelongsToRestaurant(table, restaurantId);

        if (tableRepository.existsByRestaurantIdAndLabelAndIdNot(restaurantId, request.getLabel(), tableId)) {
            throw new DomainException("A table with label '" + request.getLabel() + "' already exists");
        }

        table.setLabel(request.getLabel());
        table.setCapacity(request.getCapacity());
        table.setMinCapacity(request.getMinCapacity());
        table.setZone(request.getZone());
        table.setShape(request.getShape());
        table.setX(request.getX());
        table.setY(request.getY());
        table.setWidth(request.getWidth());
        table.setHeight(request.getHeight());
        table.setRotation(request.getRotation());
        table.setActive(Boolean.TRUE.equals(request.getActive()));
        table.validateInvariants();

        return toResponse(tableRepository.save(table));
    }

    @Override
    @Transactional
    public void delete(Long restaurantId, Long tableId, Long requesterId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        assertRestaurantManager(restaurant, requesterId);

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException(tableId));
        assertTableBelongsToRestaurant(table, restaurantId);

        // Soft delete: preserve historical reservations that reference this table.
        table.setActive(false);
        tableRepository.save(table);
        log.info("Table soft-deleted: id={} restaurant={}", tableId, restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> listByRestaurant(Long restaurantId, Long requesterId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        assertRestaurantManager(restaurant, requesterId);

        return tableRepository.findByRestaurantId(restaurantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantTableResponse getById(Long restaurantId, Long tableId, Long requesterId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        assertRestaurantManager(restaurant, requesterId);

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException(tableId));
        assertTableBelongsToRestaurant(table, restaurantId);
        return toResponse(table);
    }

    private void assertRestaurantManager(Restaurant restaurant, Long requesterId) {
        var user = userRepository.findById(requesterId)
                .orElseThrow(() -> new DomainException("Requester not found"));
        boolean isAdmin = GlobalRole.ADMIN.equals(user.getGlobalRole());
        boolean isOwner = requesterId.equals(restaurant.getOwnerId());
        if (!isAdmin && !isOwner) {
            throw new DomainException("Only the restaurant owner or admin can manage tables");
        }
    }

    private void assertTableBelongsToRestaurant(RestaurantTable table, Long restaurantId) {
        if (!restaurantId.equals(table.getRestaurantId())) {
            throw new DomainException("Table does not belong to the specified restaurant");
        }
    }

    private RestaurantTableResponse toResponse(RestaurantTable t) {
        return RestaurantTableResponse.builder()
                .id(t.getId())
                .restaurantId(t.getRestaurantId())
                .label(t.getLabel())
                .capacity(t.getCapacity())
                .minCapacity(t.getMinCapacity())
                .zone(t.getZone())
                .shape(t.getShape())
                .x(t.getX())
                .y(t.getY())
                .width(t.getWidth())
                .height(t.getHeight())
                .rotation(t.getRotation())
                .active(t.isActive())
                .build();
    }
}
