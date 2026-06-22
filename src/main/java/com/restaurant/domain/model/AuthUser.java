package com.restaurant.domain.model;

import com.restaurant.domain.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    private UserId id;
    private String email;
    private String username;
    private String name;
    private String tlf;
    private String password;
    private GlobalRole globalRole;
    private RestaurantRole restaurantRole;
    private boolean emailVerified;

    public boolean isAdmin() {
        return GlobalRole.ADMIN.equals(this.globalRole);
    }

    public boolean isRestaurantOwner() {
        return RestaurantRole.OWNER.equals(this.restaurantRole);
    }

    public boolean isRestaurantWorker() {
        return RestaurantRole.WORKER.equals(this.restaurantRole);
    }
}
