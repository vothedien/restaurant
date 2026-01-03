package com.restaurant.dto;

import com.restaurant.enums.OrderStatus;

public record SubmitOrderResponse(
        Long orderId,
        Long tableId,
        OrderStatus status,
        String message
) {}
