package com.restaurant.controller;

import com.restaurant.dto.TableDto;
import com.restaurant.service.TableService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<TableDto> listTables() {
        return tableService.listTables();
    }

    @PostMapping("/{tableId}/open")
    public TableDto openTable(@PathVariable Long tableId) {
        return tableService.openTable(tableId);
    }

    @PostMapping("/{tableId}/request-bill")
    public TableDto requestBill(@PathVariable Long tableId) {
        return tableService.requestBill(tableId);
    }

    @PostMapping("/{tableId}/set-cleaning")
    public TableDto setCleaning(@PathVariable Long tableId) {
        return tableService.setCleaning(tableId);
    }

    @PostMapping("/{tableId}/set-available")
    public TableDto setAvailable(@PathVariable Long tableId) {
        return tableService.setAvailable(tableId);
    }
}
