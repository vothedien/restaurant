package com.restaurant.dto;

import com.restaurant.enums.ItemStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateItemStatusRequest(
        @NotNull ItemStatus newStatus,
        String cancelReason
) {}
