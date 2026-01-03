package com.restaurant.dto;

import java.math.BigDecimal;

public record CheckoutResponse(
        Long paymentId,
        Long orderId,
        BigDecimal totalAmount,
        String message
) {}
