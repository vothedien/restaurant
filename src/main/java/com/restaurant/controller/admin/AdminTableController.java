package com.restaurant.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.dto.ActionResponse;
import com.restaurant.dto.admin.AdminTableDto;
import com.restaurant.dto.admin.CreateTableRequest;
import com.restaurant.dto.admin.UpdateTableRequest;
import com.restaurant.service.admin.AdminTableService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/tables")
public class AdminTableController {

    private final AdminTableService adminTableService;

    public AdminTableController(AdminTableService adminTableService) {
        this.adminTableService = adminTableService;
    }

    @GetMapping
    public List<AdminTableDto> list() {
        return adminTableService.list();
    }

    @PostMapping
    public AdminTableDto create(@Valid @RequestBody CreateTableRequest req) {
        return adminTableService.create(req);
    }

    @PutMapping("/{id}")
    public AdminTableDto update(@PathVariable Long id, @Valid @RequestBody UpdateTableRequest req) {
        return adminTableService.update(id, req);
    }
    @DeleteMapping("/{id}")
public ActionResponse delete(@PathVariable Long id) {
    adminTableService.delete(id);
    return new ActionResponse("Đã xoá bàn.");
}
    
}
