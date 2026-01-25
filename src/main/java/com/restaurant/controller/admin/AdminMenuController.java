package com.restaurant.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.dto.admin.AdminCategoryDto;
import com.restaurant.dto.admin.AdminMenuItemDto;
import com.restaurant.dto.admin.CreateCategoryRequest;
import com.restaurant.dto.admin.CreateMenuItemRequest;
import com.restaurant.dto.admin.UpdateMenuItemRequest;
import com.restaurant.service.admin.AdminMenuService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    public AdminMenuController(AdminMenuService adminMenuService) {
        this.adminMenuService = adminMenuService;
    }

    // Categories
    @GetMapping("/categories")
    public List<AdminCategoryDto> listCategories() {
        return adminMenuService.listCategories();
    }

    @PostMapping("/categories")
    public AdminCategoryDto createCategory(@Valid @RequestBody CreateCategoryRequest req) {
        return adminMenuService.createCategory(req);
    }

    // Menu items
    @GetMapping("/menu-items")
    public List<AdminMenuItemDto> listMenuItems() {
        return adminMenuService.listMenuItems();
    }

    @PostMapping("/menu-items")
    public AdminMenuItemDto createMenuItem(@Valid @RequestBody CreateMenuItemRequest req) {
        return adminMenuService.createMenuItem(req);
    }

    @PutMapping("/menu-items/{id}")
    public AdminMenuItemDto updateMenuItem(@PathVariable Long id, @Valid @RequestBody UpdateMenuItemRequest req) {
        return adminMenuService.updateMenuItem(id, req);
    }
}
