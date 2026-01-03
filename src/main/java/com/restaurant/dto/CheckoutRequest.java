package com.restaurant.dto;

import java.math.BigDecimal;

import com.restaurant.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull PaymentMethod method,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal serviceFeeAmount
) {}
