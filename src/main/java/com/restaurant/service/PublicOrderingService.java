package com.restaurant.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.dto.SubmitOrderRequest;
import com.restaurant.dto.SubmitOrderResponse;
import com.restaurant.entity.MenuItemEntity;
import com.restaurant.entity.OrderEntity;
import com.restaurant.entity.OrderItemEntity;
import com.restaurant.entity.TableEntity;
import com.restaurant.enums.ItemStatus;
import com.restaurant.enums.OrderStatus;
import com.restaurant.enums.TableStatus;
import com.restaurant.exception.BusinessRuleException;
import com.restaurant.exception.NotFoundException;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.TableRepository;

@Service
public class PublicOrderingService {

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;

    public PublicOrderingService(
            TableRepository tableRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            MenuItemRepository menuItemRepository
    ) {
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public SubmitOrderResponse submitByToken(String token, SubmitOrderRequest req) {
        TableEntity table = tableRepository.findByQrToken(token)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn với token=" + token));

        // Chặn các trạng thái không phù hợp để gọi món
        if (table.getStatus() == TableStatus.CLEANING || table.getStatus() == TableStatus.REQUESTING_BILL) {
            throw new BusinessRuleException("Bàn hiện không thể gọi món. Trạng thái: " + table.getStatus());
        }

        // Nếu bàn đang trống mà khách vào link -> coi như bắt đầu phục vụ
        if (table.getStatus() == TableStatus.AVAILABLE) {
            table.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
        }

        // Lấy hoặc tạo order DRAFT cho bàn
        OrderEntity order = getOrCreateDraftOrderForTable(table);

        // Xoá items cũ (vì FE submit là “bản cuối”)
        orderItemRepository.deleteByOrderId(order.getId());

        // Validate menu items tồn tại + available
        Map<Long, MenuItemEntity> menuMap = loadAndValidateMenuItems(req);

        // Tạo items mới (snapshot name/price)
        List<OrderItemEntity> newItems = req.items().stream().map(i -> {
            MenuItemEntity mi = menuMap.get(i.menuItemId());

            OrderItemEntity oi = new OrderItemEntity();
            oi.setOrderId(order.getId());
            oi.setMenuItemId(mi.getId());
            oi.setItemNameSnapshot(mi.getName());
            oi.setUnitPriceSnapshot(mi.getPrice());
            oi.setQty(i.qty());
            oi.setNote(i.note());
            oi.setStatus(ItemStatus.DRAFT);  // khách gửi -> vẫn là DRAFT, chờ waiter confirm
            oi.setCreatedAt(Instant.now());
            oi.setUpdatedAt(Instant.now());
            return oi;
        }).toList();

        orderItemRepository.saveAll(newItems);

        // Lưu ghi chú khách (nếu có)
        if (req.customerNote() != null && !req.customerNote().isBlank()) {
            order.setNote(req.customerNote());
            orderRepository.save(order);
        }

        // Gắn current_order_id nếu chưa có
        if (table.getCurrentOrderId() == null) {
            table.setCurrentOrderId(order.getId());
            tableRepository.save(table);
        }

        return new SubmitOrderResponse(
                order.getId(),
                table.getId(),
                order.getStatus(),
                "Đã gửi yêu cầu gọi món. Vui lòng chờ nhân viên xác nhận."
        );
    }

    private OrderEntity getOrCreateDraftOrderForTable(TableEntity table) {
        if (table.getCurrentOrderId() == null) {
            OrderEntity o = new OrderEntity();
            o.setTableId(table.getId());
            o.setStatus(OrderStatus.DRAFT);
            o.setCreatedAt(Instant.now());
            return orderRepository.save(o);
        }

        OrderEntity current = orderRepository.findById(table.getCurrentOrderId())
                .orElseThrow(() -> new NotFoundException("current_order_id không hợp lệ"));

        // Nếu đang có ACTIVE order -> MVP: chặn khách tự gọi thêm, yêu cầu gọi nhân viên
        if (current.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleException("Bàn đang có order " + current.getStatus() + ". Vui lòng gọi nhân viên để thêm món.");
        }

        return current;
    }

    private Map<Long, MenuItemEntity> loadAndValidateMenuItems(SubmitOrderRequest req) {
        List<Long> ids = req.items().stream().map(x -> x.menuItemId()).distinct().toList();
        List<MenuItemEntity> found = menuItemRepository.findAllById(ids);

        if (found.size() != ids.size()) {
            Set<Long> foundIds = found.stream().map(MenuItemEntity::getId).collect(Collectors.toSet());
            List<Long> missing = ids.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new BusinessRuleException("Món không tồn tại: " + missing);
        }

        for (MenuItemEntity mi : found) {
            if (mi.getIsAvailable() == null || !mi.getIsAvailable()) {
                throw new BusinessRuleException("Món đang hết hàng/không khả dụng: " + mi.getName());
            }
        }

        return found.stream().collect(Collectors.toMap(MenuItemEntity::getId, x -> x));
    }
}
