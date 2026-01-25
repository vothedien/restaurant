package com.restaurant.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateTableRequest(
        @NotBlank String code,
        @Min(1) Integer capacity,
        String qrToken // optional, nếu null thì auto-generate
) {}
