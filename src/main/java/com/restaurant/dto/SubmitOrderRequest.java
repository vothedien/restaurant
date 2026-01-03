package com.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitOrderRequest(
        String customerNote,
        @Valid @NotEmpty List<DraftItemRequest> items
) {}
