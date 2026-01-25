package com.restaurant.dto.admin;

import java.math.BigDecimal;

public record AdminMenuItemDto(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        BigDecimal price,
        Boolean isAvailable,
        String imageUrl
) {}
