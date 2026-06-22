package com.restaurant.infrastructure.config;

import com.restaurant.domain.model.Restaurant;
import com.restaurant.domain.model.RestaurantTable;
import com.restaurant.domain.model.TableShape;
import com.restaurant.domain.repository.RestaurantRepository;
import com.restaurant.domain.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Backfills a default table for every existing restaurant that has no tables yet,
 * preserving capacity-based reservations created before the floor-plan migration.
 * Runs once on startup; subsequent runs are no-ops for restaurants that already
 * have tables defined.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultTableSeeder implements CommandLineRunner {

    private static final String DEFAULT_LABEL = "Mesa principal";

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;

    @Override
    @Transactional
    public void run(String... args) {
        int created = 0;
        for (Restaurant restaurant : restaurantRepository.findAll()) {
            if (!tableRepository.findByRestaurantId(restaurant.getId()).isEmpty()) {
                continue;
            }
            int capacity = restaurant.getSize() != null && restaurant.getSize() > 0
                    ? restaurant.getSize()
                    : 1;

            RestaurantTable table = RestaurantTable.builder()
                    .restaurantId(restaurant.getId())
                    .label(DEFAULT_LABEL)
                    .capacity(capacity)
                    .zone("INTERIOR")
                    .shape(TableShape.RECTANGLE)
                    .x(0).y(0).width(200).height(100).rotation(0)
                    .active(true)
                    .build();
            table.validateInvariants();
            tableRepository.save(table);
            created++;
        }
        if (created > 0) {
            log.info("DefaultTableSeeder: created {} default table(s)", created);
        }
    }
}
