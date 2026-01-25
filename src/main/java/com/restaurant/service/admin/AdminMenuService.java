package com.restaurant.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.dto.admin.AdminCategoryDto;
import com.restaurant.dto.admin.AdminMenuItemDto;
import com.restaurant.dto.admin.CreateCategoryRequest;
import com.restaurant.dto.admin.CreateMenuItemRequest;
import com.restaurant.dto.admin.UpdateMenuItemRequest;
import com.restaurant.entity.MenuCategoryEntity;
import com.restaurant.entity.MenuItemEntity;
import com.restaurant.exception.BusinessRuleException;
import com.restaurant.exception.NotFoundException;
import com.restaurant.repository.MenuCategoryRepository;
import com.restaurant.repository.MenuItemRepository;

@Service
public class AdminMenuService {

    private final MenuCategoryRepository categoryRepo;
    private final MenuItemRepository itemRepo;

    public AdminMenuService(MenuCategoryRepository categoryRepo, MenuItemRepository itemRepo) {
        this.categoryRepo = categoryRepo;
        this.itemRepo = itemRepo;
    }

    public List<AdminCategoryDto> listCategories() {
        return categoryRepo.findAll().stream()
                .map(c -> new AdminCategoryDto(c.getId(), c.getName()))
                .toList();
    }

    @Transactional
    public AdminCategoryDto createCategory(CreateCategoryRequest req) {
        String name = req.name().trim();
        if (categoryRepo.findByNameIgnoreCase(name).isPresent()) {
            throw new BusinessRuleException("Category đã tồn tại: " + name);
        }

        MenuCategoryEntity c = new MenuCategoryEntity();
        c.setName(name);
        MenuCategoryEntity saved = categoryRepo.save(c);
        return new AdminCategoryDto(saved.getId(), saved.getName());
    }

    public List<AdminMenuItemDto> listMenuItems() {
        // map category id -> name để trả về kèm categoryName
        var catMap = categoryRepo.findAll().stream().collect(java.util.stream.Collectors.toMap(
                MenuCategoryEntity::getId, MenuCategoryEntity::getName
        ));

        return itemRepo.findAll().stream().map(m -> new AdminMenuItemDto(
                m.getId(),
                m.getCategoryId(),
                catMap.getOrDefault(m.getCategoryId(), ""),
                m.getName(),
                m.getPrice(),
                m.getIsAvailable(),
                m.getImageUrl()
        )).toList();
    }

    @Transactional
    public AdminMenuItemDto createMenuItem(CreateMenuItemRequest req) {
        MenuCategoryEntity cat = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy categoryId=" + req.categoryId()));

        MenuItemEntity m = new MenuItemEntity();
        m.setCategoryId(cat.getId());
        m.setName(req.name().trim());
        m.setPrice(req.price());
        m.setIsAvailable(req.isAvailable() == null ? true : req.isAvailable());
        m.setImageUrl(req.imageUrl());

        MenuItemEntity saved = itemRepo.save(m);
        return new AdminMenuItemDto(
                saved.getId(), saved.getCategoryId(), cat.getName(),
                saved.getName(), saved.getPrice(), saved.getIsAvailable(), saved.getImageUrl()
        );
    }

    @Transactional
    public AdminMenuItemDto updateMenuItem(Long id, UpdateMenuItemRequest req) {
        MenuItemEntity m = itemRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy menu item id=" + id));

        MenuCategoryEntity cat = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy categoryId=" + req.categoryId()));

        m.setCategoryId(cat.getId());
        m.setName(req.name().trim());
        m.setPrice(req.price());
        m.setIsAvailable(req.isAvailable());
        m.setImageUrl(req.imageUrl());

        MenuItemEntity saved = itemRepo.save(m);
        return new AdminMenuItemDto(
                saved.getId(), saved.getCategoryId(), cat.getName(),
                saved.getName(), saved.getPrice(), saved.getIsAvailable(), saved.getImageUrl()
        );
    }
}
