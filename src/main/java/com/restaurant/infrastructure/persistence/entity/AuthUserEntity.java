package com.restaurant.infrastructure.persistence.entity;

import com.restaurant.domain.model.GlobalRole;
import com.restaurant.domain.model.RestaurantRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "username")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String tlf;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GlobalRole globalRole;

    @Enumerated(EnumType.STRING)
    private RestaurantRole restaurantRole;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean emailVerified;
}
