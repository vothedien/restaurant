package com.restaurant.dto.admin;

import com.restaurant.enums.TableStatus;

public record AdminTableDto(
        Long id,
        String code,
        Integer capacity,
        TableStatus status,
        String qrToken
) {}
