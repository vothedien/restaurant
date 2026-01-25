package com.restaurant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurant.entity.TableEntity;

public interface TableRepository extends JpaRepository<TableEntity, Long> {
    Optional<TableEntity> findByQrToken(String qrToken);

    boolean existsByCode(String code);
    boolean existsByQrToken(String qrToken);
}
