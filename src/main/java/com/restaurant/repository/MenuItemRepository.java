package com.restaurant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.entity.MenuItemEntity;

public interface MenuItemRepository extends JpaRepository<MenuItemEntity, Long> {

    List<MenuItemEntity> findByIsAvailableTrueOrderByIdAsc();

    Optional<MenuItemEntity> findByIdAndIsAvailableTrue(Long id);
}
