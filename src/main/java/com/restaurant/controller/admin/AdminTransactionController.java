package com.restaurant.controller.admin;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.dto.admin.transaction.InvoiceDto;
import com.restaurant.dto.admin.transaction.TransactionSummaryDto;
import com.restaurant.service.admin.AdminTransactionService;

@RestController
@RequestMapping("/api/admin")
public class AdminTransactionController {

    private final AdminTransactionService adminTransactionService;

    public AdminTransactionController(AdminTransactionService adminTransactionService) {
        this.adminTransactionService = adminTransactionService;
    }

    @GetMapping("/transactions")
    public Page<TransactionSummaryDto> transactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminTransactionService.listTransactions(from, to, page, size);
    }

    @GetMapping("/invoices/{paymentId}")
    public InvoiceDto invoice(@PathVariable Long paymentId) {
        return adminTransactionService.getInvoice(paymentId);
    }
}
