package com.restaurant.dto.admin;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateMenuItemRequest(
        @NotNull Long categoryId,
        @NotBlank String name,
        @NotNull @Min(0) BigDecimal price,
        @NotNull Boolean isAvailable,
        String imageUrl
) {}
