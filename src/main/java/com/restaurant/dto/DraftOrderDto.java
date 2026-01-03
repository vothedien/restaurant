package com.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;

import com.restaurant.enums.ItemStatus;
import com.restaurant.enums.OrderStatus;

public record DraftOrderDto(
        Long orderId,
        Long tableId,
        OrderStatus status,
        String note,
        List<DraftOrderItemDto> items
) {
    public record DraftOrderItemDto(
            Long itemId,
            Long menuItemId,
            String name,
            BigDecimal unitPrice,
            Integer qty,
            String note,
            ItemStatus status
    ) {}
}
