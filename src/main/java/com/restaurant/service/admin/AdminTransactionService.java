package com.restaurant.service.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.restaurant.dto.admin.transaction.InvoiceDto;
import com.restaurant.dto.admin.transaction.TransactionSummaryDto;
import com.restaurant.entity.OrderEntity;
import com.restaurant.entity.OrderItemEntity;
import com.restaurant.entity.PaymentEntity;
import com.restaurant.entity.TableEntity;
import com.restaurant.entity.UserEntity;
import com.restaurant.exception.NotFoundException;
import com.restaurant.repository.OrderItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.PaymentRepository;
import com.restaurant.repository.TableRepository;
import com.restaurant.repository.UserRepository;

@Service
public class AdminTransactionService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;

    public AdminTransactionService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            TableRepository tableRepository,
            UserRepository userRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
    }

    public Page<TransactionSummaryDto> listTransactions(LocalDateTime from, LocalDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paidAt"));

        Page<PaymentEntity> p;
        if (from != null && to != null) {
            p = paymentRepository.findByPaidAtBetween(from, to, pageable);
        } else if (from != null) {
            p = paymentRepository.findByPaidAtAfter(from, pageable);
        } else if (to != null) {
            p = paymentRepository.findByPaidAtBefore(to, pageable);
        } else {
            p = paymentRepository.findAll(pageable);
        }

        return p.map(pay -> {
            OrderEntity order = orderRepository.findById(pay.getOrderId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy order_id=" + pay.getOrderId()));

            TableEntity table = tableRepository.findById(order.getTableId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy table_id=" + order.getTableId()));

            String cashierUsername = null;
            if (pay.getCashierUserId() != null) {
                cashierUsername = userRepository.findById(pay.getCashierUserId())
                        .map(UserEntity::getUsername)
                        .orElse(null);
            }

            return new TransactionSummaryDto(
                    pay.getId(),
                    pay.getOrderId(),
                    order.getTableId(),
                    table.getCode(),
                    pay.getTotalAmount(),
                    pay.getMethod().name(),
                    pay.getPaidAt(),
                    pay.getCashierUserId(),
                    cashierUsername
            );
        });
    }

    public InvoiceDto getInvoice(Long paymentId) {
        PaymentEntity pay = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy payment_id=" + paymentId));

        OrderEntity order = orderRepository.findById(pay.getOrderId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy order_id=" + pay.getOrderId()));

        TableEntity table = tableRepository.findById(order.getTableId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy table_id=" + order.getTableId()));

        String cashierUsername = null;
        if (pay.getCashierUserId() != null) {
            cashierUsername = userRepository.findById(pay.getCashierUserId())
                    .map(UserEntity::getUsername)
                    .orElse(null);
        }

        List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());

        List<InvoiceDto.InvoiceItemDto> itemDtos = items.stream().map(it -> {
            BigDecimal lineTotal = it.getStatus().name().equals("CANCELED")
                    ? BigDecimal.ZERO
                    : it.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(it.getQty()));

            return new InvoiceDto.InvoiceItemDto(
                    it.getId(),
                    it.getItemNameSnapshot(),
                    it.getUnitPriceSnapshot(),
                    it.getQty(),
                    lineTotal,
                    it.getStatus().name(),
                    it.getNote()
            );
        }).toList();

        return new InvoiceDto(
                pay.getId(),
                pay.getOrderId(),
                order.getTableId(),
                table.getCode(),

                pay.getSubtotal(),
                pay.getDiscountAmount(),
                pay.getTaxAmount(),
                pay.getServiceFeeAmount(),
                pay.getTotalAmount(),

                pay.getMethod().name(),
                pay.getPaidAt(),

                pay.getCashierUserId(),
                cashierUsername,

                itemDtos
        );
    }
}
