package com.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;

import com.restaurant.enums.ItemStatus;
import com.restaurant.enums.OrderStatus;

public record OrderDetailDto(
        Long orderId,
        Long tableId,
        OrderStatus status,
        String note,
        List<ItemDto> items
) {
    public record ItemDto(
            Long itemId,
            Long menuItemId,
            String name,
            BigDecimal unitPrice,
            Integer qty,
            String note,
            ItemStatus status
    ) {}
}
