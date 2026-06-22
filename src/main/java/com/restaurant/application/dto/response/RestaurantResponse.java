package com.restaurant.application.dto.response;

import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {

    private Long id;
    private String name;
    private String email;
    private String tlf;
    private Integer size;
    private BigDecimal averagePrice;
    private CuisineType cuisineType;
    private Set<DietaryOption> dietaryOptions;
    private LocationResponse location;
    private List<Long> menuIds;
    private Long ownerId;
}
