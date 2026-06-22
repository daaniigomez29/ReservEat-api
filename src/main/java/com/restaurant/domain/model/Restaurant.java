package com.restaurant.domain.model;

import com.restaurant.domain.valueobject.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    private Long id;
    private String name;
    private String email;
    private String tlf;
    private Integer size;                   // total capacity
    private BigDecimal averagePrice;
    private Location location;
    private CuisineType cuisineType;
    private Set<DietaryOption> dietaryOptions;

    @Builder.Default
    private List<Long> menuIds = new ArrayList<>();

    private Long ownerId;

    public boolean hasCapacity(int partySize) {
        return this.size != null && this.size >= partySize;
    }
}
