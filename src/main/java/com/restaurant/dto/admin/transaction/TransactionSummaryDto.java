package com.restaurant.dto.admin.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionSummaryDto(
        Long paymentId,
        Long orderId,
        Long tableId,
        String tableCode,
        BigDecimal totalAmount,
        String method,
        LocalDateTime paidAt,
        Long cashierUserId,
        String cashierUsername
) {}
