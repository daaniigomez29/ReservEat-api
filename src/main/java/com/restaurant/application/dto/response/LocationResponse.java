package com.restaurant.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {

    private String street;
    private String city;
    private String province;
    private String postalCode;
    private BigDecimal lat;
    private BigDecimal lon;
}
