package com.restaurant.repository;

import com.restaurant.entity.OrderEntity;
import com.restaurant.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByTableIdAndStatusIn(Long tableId, List<OrderStatus> statuses);
}
