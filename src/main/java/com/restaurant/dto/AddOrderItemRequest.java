package com.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddOrderItemRequest(
        @NotNull Long menuItemId,
        @NotNull @Min(1) Integer qty,
        String note
) {}
