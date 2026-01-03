package com.restaurant.dto;

import com.restaurant.enums.TableStatus;

public record TableDto(
        Long id,
        String code,
        Integer capacity,
        TableStatus status,
        Long currentOrderId
) {}
