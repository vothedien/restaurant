package com.restaurant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.entity.MenuCategoryEntity;

public interface MenuCategoryRepository extends JpaRepository<MenuCategoryEntity, Long> {
    Optional<MenuCategoryEntity> findByNameIgnoreCase(String name);
}
