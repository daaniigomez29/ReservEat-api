package com.restaurant.domain.model;

import com.restaurant.domain.valueobject.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
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

    // Service window during which reservations are accepted. A closing time
    // earlier than the opening time denotes a window crossing midnight
    // (e.g. 19:00–02:00). When either is null, opening hours are unrestricted.
    private LocalTime openingTime;
    private LocalTime closingTime;

    public boolean hasCapacity(int partySize) {
        return this.size != null && this.size >= partySize;
    }

    /**
     * Whether the given time-of-day falls within the restaurant's opening hours.
     * Unset hours mean no restriction; an opening time equal to the closing time
     * is treated as open around the clock.
     */
    public boolean isWithinOpeningHours(LocalTime time) {
        if (openingTime == null || closingTime == null || openingTime.equals(closingTime)) {
            return true;
        }
        if (openingTime.isBefore(closingTime)) {
            // Same-day window: [opening, closing)
            return !time.isBefore(openingTime) && time.isBefore(closingTime);
        }
        // Overnight window: open from opening until midnight, and from midnight until closing.
        return !time.isBefore(openingTime) || time.isBefore(closingTime);
    }

    public boolean isOwnerOrAdmin(AuthUser user) {
        return user != null && 
        (this.ownerId.equals(user.getId().value()) || user.isAdmin());
    }
}
