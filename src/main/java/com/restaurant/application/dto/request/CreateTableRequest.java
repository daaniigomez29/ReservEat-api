package com.restaurant.application.dto.request;

import com.restaurant.domain.model.TableShape;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTableRequest {

    @NotBlank
    @Size(min = 1, max = 32)
    private String label;

    @Positive
    private int capacity;

    @Positive
    private Integer minCapacity;

    @Size(max = 32)
    private String zone;

    @NotNull
    private TableShape shape;

    @PositiveOrZero @Max(1000)
    private int x;

    @PositiveOrZero @Max(1000)
    private int y;

    @Positive @Max(1000)
    private int width;

    @Positive @Max(1000)
    private int height;

    @PositiveOrZero @Max(359)
    private int rotation;
}
