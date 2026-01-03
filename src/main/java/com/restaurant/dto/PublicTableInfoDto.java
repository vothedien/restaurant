package com.restaurant.dto;

import com.restaurant.enums.TableStatus;

public record PublicTableInfoDto(
        Long tableId,
        String code,
        TableStatus status
) {}
