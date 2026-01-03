package com.restaurant.service;

import com.restaurant.dto.TableDto;
import com.restaurant.entity.OrderEntity;
import com.restaurant.entity.TableEntity;
import com.restaurant.enums.OrderStatus;
import com.restaurant.enums.TableStatus;
import com.restaurant.exception.BusinessRuleException;
import com.restaurant.exception.NotFoundException;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.TableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TableService {

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;

    public TableService(TableRepository tableRepository, OrderRepository orderRepository) {
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
    }

    public List<TableDto> listTables() {
        return tableRepository.findAll().stream()
                .map(t -> new TableDto(t.getId(), t.getCode(), t.getCapacity(), t.getStatus(), t.getCurrentOrderId()))
                .toList();
    }

    @Transactional
    public TableDto openTable(Long tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn id=" + tableId));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new BusinessRuleException("Chỉ mở được bàn khi trạng thái AVAILABLE. Hiện tại: " + table.getStatus());
        }

        // Đổi trạng thái bàn
        table.setStatus(TableStatus.OCCUPIED);

        // Nếu chưa có order hiện tại thì tạo order ACTIVE trống (để waiter thêm món)
        if (table.getCurrentOrderId() == null) {
            OrderEntity order = new OrderEntity();
            order.setTableId(table.getId());
            order.setStatus(OrderStatus.ACTIVE);
            order.setCreatedAt(Instant.now());
            order.setConfirmedAt(LocalDateTime.now()); // vì mở bàn bởi nhân viên -> order có hiệu lực
            OrderEntity saved = orderRepository.save(order);

            table.setCurrentOrderId(saved.getId());
        }

        TableEntity savedTable = tableRepository.save(table);

        return new TableDto(savedTable.getId(), savedTable.getCode(), savedTable.getCapacity(),
                savedTable.getStatus(), savedTable.getCurrentOrderId());
    }

    @Transactional
    public TableDto requestBill(Long tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn id=" + tableId));

        if (table.getStatus() != TableStatus.OCCUPIED) {
            throw new BusinessRuleException("Chỉ yêu cầu tính tiền khi bàn OCCUPIED. Hiện tại: " + table.getStatus());
        }

        if (table.getCurrentOrderId() == null) {
            throw new BusinessRuleException("Bàn chưa có order hiện tại nên không thể yêu cầu tính tiền.");
        }

        table.setStatus(TableStatus.REQUESTING_BILL);
        TableEntity saved = tableRepository.save(table);

        return new TableDto(saved.getId(), saved.getCode(), saved.getCapacity(), saved.getStatus(), saved.getCurrentOrderId());
    }

    @Transactional
    public TableDto setCleaning(Long tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn id=" + tableId));

        if (table.getStatus() != TableStatus.REQUESTING_BILL) {
            throw new BusinessRuleException("Chỉ chuyển CLEANING khi bàn REQUESTING_BILL. Hiện tại: " + table.getStatus());
        }

        table.setStatus(TableStatus.CLEANING);
        TableEntity saved = tableRepository.save(table);

        return new TableDto(saved.getId(), saved.getCode(), saved.getCapacity(), saved.getStatus(), saved.getCurrentOrderId());
    }

    @Transactional
    public TableDto setAvailable(Long tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn id=" + tableId));

        if (table.getStatus() != TableStatus.CLEANING) {
            throw new BusinessRuleException("Chỉ chuyển AVAILABLE khi bàn CLEANING. Hiện tại: " + table.getStatus());
        }

        // Khi bàn available lại, bạn có thể clear current_order_id (tuỳ luật)
        table.setStatus(TableStatus.AVAILABLE);
        table.setCurrentOrderId(null);

        TableEntity saved = tableRepository.save(table);
        return new TableDto(saved.getId(), saved.getCode(), saved.getCapacity(), saved.getStatus(), saved.getCurrentOrderId());
    }
}
