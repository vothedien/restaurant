package com.restaurant.dto;

import java.math.BigDecimal;

public record PublicMenuItemDto(
        Long id,
        Long categoryId,
        String name,
        BigDecimal price,
        Boolean isAvailable,
        String imageUrl
) {}
