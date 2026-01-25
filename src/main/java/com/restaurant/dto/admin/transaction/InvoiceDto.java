package com.restaurant.dto.admin.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceDto(
        Long paymentId,
        Long orderId,
        Long tableId,
        String tableCode,

        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal serviceFeeAmount,
        BigDecimal totalAmount,

        String method,
        LocalDateTime paidAt,

        Long cashierUserId,
        String cashierUsername,

        List<InvoiceItemDto> items
) {
    public record InvoiceItemDto(
            Long itemId,
            String name,
            BigDecimal unitPrice,
            Integer qty,
            BigDecimal lineTotal,
            String status,
            String note
    ) {}
}
