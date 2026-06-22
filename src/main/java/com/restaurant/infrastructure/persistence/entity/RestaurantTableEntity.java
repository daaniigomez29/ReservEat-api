package com.restaurant.infrastructure.persistence.entity;

import com.restaurant.domain.model.TableShape;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_tables",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_table_restaurant_label",
                columnNames = {"restaurant_id", "label"}),
        indexes = {
                @Index(name = "idx_table_restaurant", columnList = "restaurant_id"),
                @Index(name = "idx_table_restaurant_active", columnList = "restaurant_id, active")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(nullable = false, length = 32)
    private String label;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "min_capacity")
    private Integer minCapacity;

    @Column(length = 32)
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TableShape shape;

    @Column(nullable = false)
    private int x;

    @Column(nullable = false)
    private int y;

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    @Column(nullable = false)
    private int rotation;

    @Column(nullable = false)
    private boolean active;
}
