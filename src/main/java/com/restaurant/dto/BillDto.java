package com.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;

import com.restaurant.enums.ItemStatus;
import com.restaurant.enums.OrderStatus;

public record BillDto(
        Long orderId,
        Long tableId,
        OrderStatus orderStatus,
        List<BillItemDto> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal serviceFeeAmount,
        BigDecimal totalAmount
) {
    public record BillItemDto(
            Long itemId,
            String name,
            BigDecimal unitPrice,
            Integer qty,
            BigDecimal lineTotal,
            ItemStatus status
    ) {}
}
