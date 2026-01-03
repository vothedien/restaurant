package com.restaurant.controller;

import com.restaurant.entity.MenuItemEntity;
import com.restaurant.repository.MenuItemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MenuController {

    private final MenuItemRepository menuItemRepository;

    public MenuController(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @GetMapping("/api/menu")
    public List<MenuItemEntity> getMenu() {
        return menuItemRepository.findByIsAvailableTrueOrderByIdAsc();
    }
}
