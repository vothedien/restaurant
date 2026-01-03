package com.restaurant.entity;
import com.restaurant.enums.TableStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tables")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private Integer capacity = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(name = "qr_token", nullable = false, unique = true, length = 64)
    private String qrToken;

    // FK tới orders.id (nullable). Để đơn giản, map dạng field + relation.
    @Column(name = "current_order_id")
    private Long currentOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_order_id", insertable = false, updatable = false)
    private OrderEntity currentOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
