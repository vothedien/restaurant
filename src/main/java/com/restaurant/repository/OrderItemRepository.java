package com.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    void deleteByOrderId(Long orderId);

    List<OrderItemEntity> findByOrderIdOrderByIdAsc(Long orderId);
}
