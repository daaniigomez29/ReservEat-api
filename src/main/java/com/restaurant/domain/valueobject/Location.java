package com.restaurant.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private String street;
    private String city;
    private String province;
    private String postalCode;
    private BigDecimal lat;
    private BigDecimal lon;

    public String fullAddress() {
        return String.format("%s, %s, %s %s", street, city, province, postalCode);
    }
}
