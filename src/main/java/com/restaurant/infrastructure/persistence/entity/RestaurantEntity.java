package com.restaurant.infrastructure.persistence.entity;

import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String tlf;

    @Column(nullable = false)
    private Integer size;

    @Column(precision = 10, scale = 2)
    private BigDecimal averagePrice;

    @Enumerated(EnumType.STRING)
    private CuisineType cuisineType;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @OneToMany(mappedBy = "restaurantEntity")
    private List<MenuEntity> menuEntityList;

    @ElementCollection(targetClass = DietaryOption.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "restaurant_dietary_options",
            joinColumns = @JoinColumn(name = "restaurant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_option")
    @Builder.Default
    private Set<DietaryOption> dietaryOptions = new HashSet<>();

    // Embedded location
    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "province")
    private String province;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "lat", precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(name = "lon", precision = 10, scale = 7)
    private BigDecimal lon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AuthUserEntity owner;
}
