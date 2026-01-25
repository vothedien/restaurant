package com.restaurant.dto.admin;

import com.restaurant.enums.TableStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTableRequest(
        @NotBlank String code,
        @Min(1) Integer capacity,
        @NotNull TableStatus status
) {}
