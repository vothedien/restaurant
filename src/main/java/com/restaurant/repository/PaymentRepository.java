package com.restaurant.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(Long orderId);

    Page<PaymentEntity> findByPaidAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<PaymentEntity> findByPaidAtAfter(LocalDateTime from, Pageable pageable);
    Page<PaymentEntity> findByPaidAtBefore(LocalDateTime to, Pageable pageable);
}
