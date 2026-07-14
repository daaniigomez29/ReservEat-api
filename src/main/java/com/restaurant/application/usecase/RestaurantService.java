package com.restaurant.application.usecase;

import com.restaurant.application.dto.request.CreateRestaurantRequest;
import com.restaurant.application.dto.response.LocationResponse;
import com.restaurant.application.dto.response.RestaurantResponse;
import com.restaurant.application.port.in.RestaurantUseCase;
import com.restaurant.domain.exception.DomainException;
import com.restaurant.domain.exception.RestaurantNotFoundException;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;
import com.restaurant.domain.model.Restaurant;
import com.restaurant.domain.repository.RestaurantRepository;
import com.restaurant.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService implements RestaurantUseCase {

    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request, Long ownerId) {
        Location location = Location.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .province(request.getProvince())
                .postalCode(request.getPostalCode())
                .lat(request.getLat())
                .lon(request.getLon())
                .build();


        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .tlf(request.getTlf())
                .size(request.getSize())
                .averagePrice(request.getAveragePrice())
                .cuisineType(request.getCuisineType())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .dietaryOptions(request.getDietaryOptions())
                .location(location)
                .ownerId(ownerId)
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant created: {} by owner {}", saved.getName(), ownerId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        return toResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> searchRestaurants(String name, String city, String province,
                                                      CuisineType cuisineType,
                                                      DietaryOption dietaryOption,
                                                      BigDecimal maxPrice, int page, int size) {
        Page<Restaurant> restaurants = restaurantRepository.search(name, city, province, cuisineType, dietaryOption, maxPrice, page, size);
        return restaurants.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getOwnerRestaurants(Long ownerId) {
        return restaurantRepository.getOwnerRestaurants(ownerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RestaurantResponse updateRestaurant(Long id, CreateRestaurantRequest request, AuthUser requester) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));

        assertOwnerOrAdmin(restaurant, requester);

        restaurant.setName(request.getName());
        restaurant.setEmail(request.getEmail());
        restaurant.setTlf(request.getTlf());
        restaurant.setSize(request.getSize());
        restaurant.setAveragePrice(request.getAveragePrice());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setDietaryOptions(request.getDietaryOptions());
        restaurant.setLocation(Location.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .province(request.getProvince())
                .postalCode(request.getPostalCode())
                .lat(request.getLat())
                .lon(request.getLon())
                .build());

        return toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    @Transactional
    public void deleteRestaurant(Long id, AuthUser requester) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        assertOwnerOrAdmin(restaurant, requester);
        restaurantRepository.deleteById(id);
        log.info("Restaurant {} deleted by user {}", id, requester.getId().value());
    }

    private void assertOwnerOrAdmin(Restaurant restaurant, AuthUser requester) {
        if (!restaurant.isOwnerOrAdmin(requester)) {
            throw new DomainException("You do not have permission to modify this restaurant");
        }
    }

    private RestaurantResponse toResponse(Restaurant r) {
        LocationResponse locationResponse = null;
        if (r.getLocation() != null) {
            locationResponse = LocationResponse.builder()
                    .street(r.getLocation().getStreet())
                    .city(r.getLocation().getCity())
                    .province(r.getLocation().getProvince())
                    .postalCode(r.getLocation().getPostalCode())
                    .lat(r.getLocation().getLat())
                    .lon(r.getLocation().getLon())
                    .build();
        }

        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .email(r.getEmail())
                .tlf(r.getTlf())
                .size(r.getSize())
                .averagePrice(r.getAveragePrice())
                .cuisineType(r.getCuisineType())
                .openingTime(r.getOpeningTime())
                .closingTime(r.getClosingTime())
                .dietaryOptions(r.getDietaryOptions())
                .location(locationResponse)
                .menuIds(r.getMenuIds())
                .ownerId(r.getOwnerId())
                .build();
    }
}
