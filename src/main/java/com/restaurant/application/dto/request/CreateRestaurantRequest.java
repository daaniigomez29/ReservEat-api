package com.restaurant.application.dto.request;

import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 150)
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "Invalid phone number")
    private String tlf;

    @Positive(message = "Capacity must be positive")
    private Integer size;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal averagePrice;

    @NotNull
    private CuisineType cuisineType;

    private Set<DietaryOption> dietaryOptions;

    // Location fields
    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String province;

    private String postalCode;

    @NotNull
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private BigDecimal lat;

    @NotNull
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private BigDecimal lon;
}
