package com.restaurant.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.dto.ActionResponse;
import com.restaurant.dto.AddOrderItemRequest;
import com.restaurant.dto.BillDto;
import com.restaurant.dto.CheckoutRequest;
import com.restaurant.dto.CheckoutResponse;
import com.restaurant.dto.DraftOrderDto;
import com.restaurant.dto.OrderDetailDto;
import com.restaurant.dto.UpdateItemStatusRequest;
import com.restaurant.dto.UpdateOrderItemRequest;
import com.restaurant.entity.MenuItemEntity;
import com.restaurant.entity.OrderEntity;
import com.restaurant.entity.OrderItemEntity;
import com.restaurant.entity.PaymentEntity;
import com.restaurant.entity.TableEntity;
import com.restaurant.enums.ItemStatus;
import com.restaurant.enums.OrderStatus;
import com.restaurant.enums.TableStatus;
import com.restaurant.exception.BusinessRuleException;
import com.restaurant.exception.NotFoundException;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.PaymentRepository;
import com.restaurant.repository.TableRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(OrderRepository orderRepository,
                    OrderItemRepository orderItemRepository,
                    TableRepository tableRepository,
                    MenuItemRepository menuItemRepository,
                    PaymentRepository paymentRepository) {
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.tableRepository = tableRepository;
    this.menuItemRepository = menuItemRepository;
    this.paymentRepository = paymentRepository;
}


    // =========================
    // 1) Waiter xem order DRAFT theo bàn
    // =========================
    public DraftOrderDto getDraftOrderByTable(Long tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn id=" + tableId));

        if (table.getCurrentOrderId() == null) {
            throw new NotFoundException("Bàn chưa có order nháp.");
        }

        OrderEntity order = orderRepository.findById(table.getCurrentOrderId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy order hiện tại của bàn."));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleException("Order hiện tại không phải DRAFT (đang là " + order.getStatus() + ")");
        }

        List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());

        return new DraftOrderDto(
                order.getId(),
                order.getTableId(),
                order.getStatus(),
                order.getNote(),
                items.stream().map(i -> new DraftOrderDto.DraftOrderItemDto(
                        i.getId(),
                        i.getMenuItemId(),
                        i.getItemNameSnapshot(),
                        i.getUnitPriceSnapshot(),
                        i.getQty(),
                        i.getNote(),
                        i.getStatus()
                )).toList()
        );
    }

    // =========================
    // 2) Waiter confirm: DRAFT -> ACTIVE
    // =========================
    @Transactional
    public ActionResponse confirmOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy order id=" + orderId));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleException("Chỉ xác nhận được order DRAFT. Hiện tại: " + order.getStatus());
        }

        List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
        if (items.isEmpty()) {
            throw new BusinessRuleException("Order không có món nào, không thể xác nhận.");
        }

        order.setStatus(OrderStatus.ACTIVE);
        orderRepository.save(order);

        return new ActionResponse("Đã xác nhận order. Order chuyển sang ACTIVE.");
    }

    // =========================
    // 3) CRUD món cho order ACTIVE
    // =========================

    @Transactional
    public ActionResponse addItem(Long orderId, AddOrderItemRequest req) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy order id=" + orderId));

        if (order.getStatus() != OrderStatus.ACTIVE) {
            throw new BusinessRuleException("Chỉ thêm món khi order ACTIVE. Hiện tại: " + order.getStatus());
        }

        MenuItemEntity mi = menuItemRepository.findByIdAndIsAvailableTrue(req.menuItemId())
                .orElseThrow(() -> new BusinessRuleException("Món không tồn tại hoặc đang hết hàng. id=" + req.menuItemId()));

        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(order.getId());
        item.setMenuItemId(mi.getId());
        item.setItemNameSnapshot(mi.getName());
        item.setUnitPriceSnapshot(mi.getPrice());
        item.setQty(req.qty());
        item.setNote(req.note());

        // Waiter thêm món => mặc định đang xử lý
        // Nếu bạn muốn waiter thêm rồi mới "Send to kitchen" thì đổi thành ItemStatus.DRAFT
        item.setStatus(ItemStatus.PENDING);

        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());

        orderItemRepository.save(item);
        return new ActionResponse("Đã thêm món vào order.");
    }

    @Transactional
    public ActionResponse updateItem(Long orderId, Long itemId, UpdateOrderItemRequest req) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy order id=" + orderId));

        if (order.getStatus() != OrderStatus.ACTIVE) {
            throw new BusinessRuleException("Chỉ sửa món khi order ACTIVE. Hiện tại: " + order.getStatus());
        }

        OrderItemEntity item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy item id=" + itemId));

        if (!item.getOrderId().equals(orderId)) {
            throw new BusinessRuleException("Item không thuộc order này.");
        }

        // Chặn sửa khi món đã hoàn tất/huỷ
        if (item.getStatus() == ItemStatus.READY || item.getStatus() == ItemStatus.SERVED || item.getStatus() == ItemStatus.CANCELED) {
            throw new BusinessRuleException("Không thể sửa món khi đang ở trạng thái: " + item.getStatus());
        }

        item.setQty(req.qty());
        item.setNote(req.note());
        item.setUpdatedAt(Instant.now());

        orderItemRepository.save(item);
        return new ActionResponse("Đã cập nhật món.");
    }

    @Transactional
    public ActionResponse removeItem(Long orderId, Long itemId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy order id=" + orderId));

        if (order.getStatus() != OrderStatus.ACTIVE) {
            throw new BusinessRuleException("Chỉ xoá món khi order ACTIVE. Hiện tại: " + order.getStatus());
        }

        OrderItemEntity item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy item id=" + itemId));

        if (!item.getOrderId().equals(orderId)) {
            throw new BusinessRuleException("Item không thuộc order này.");
        }

        // Chặn xoá nếu đã READY/SERVED
        if (item.getStatus() == ItemStatus.READY || item.getStatus() == ItemStatus.SERVED) {
            throw new BusinessRuleException("Không thể xoá món khi đang ở trạng thái: " + item.getStatus());
        }

        orderItemRepository.delete(item);
        return new ActionResponse("Đã xoá món khỏi order.");
    } 
    public OrderDetailDto getOrderDetail(Long orderId) {
    OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy order id=" + orderId));

    List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());

    return new OrderDetailDto(
            order.getId(),
            order.getTableId(),
            order.getStatus(),
            order.getNote(),
            items.stream().map(i -> new OrderDetailDto.ItemDto(
                    i.getId(),
                    i.getMenuItemId(),
                    i.getItemNameSnapshot(),
                    i.getUnitPriceSnapshot(),
                    i.getQty(),
                    i.getNote(),
                    i.getStatus()
            )).toList()
    );
} 
@Transactional
public ActionResponse updateItemStatus(Long orderId, Long itemId, UpdateItemStatusRequest req) {
    OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy order id=" + orderId));

    if (order.getStatus() != OrderStatus.ACTIVE) {
        throw new BusinessRuleException("Chỉ cập nhật trạng thái món khi order ACTIVE. Hiện tại: " + order.getStatus());
    }

    OrderItemEntity item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy item id=" + itemId));

    if (!item.getOrderId().equals(orderId)) {
        throw new BusinessRuleException("Item không thuộc order này.");
    }

    ItemStatus from = item.getStatus();
    ItemStatus to = req.newStatus();

    validateItemStatusTransition(from, to);

    // set timestamp theo trạng thái mới
    LocalDateTime now = LocalDateTime.now();
    switch (to) {
        case PENDING -> item.setSentAt(now);
        case COOKING -> item.setCookingAt(now);
        case READY -> item.setReadyAt(now);
        case SERVED -> item.setServedAt(now);
        case CANCELED -> {
            item.setCanceledAt(now);
            if (req.cancelReason() != null && !req.cancelReason().isBlank()) {
                item.setCanceledReason(req.cancelReason());
            } else if (item.getCanceledReason() == null) {
                item.setCanceledReason("Không có lý do");
            }
        }
        default -> {  }
    }

    item.setStatus(to);
    item.setUpdatedAt(Instant.now());
    orderItemRepository.save(item);

    return new ActionResponse("Đã đổi trạng thái món từ " + from + " -> " + to);
}

private void validateItemStatusTransition(ItemStatus from, ItemStatus to) {
    if (from == to) return;

    if (to == ItemStatus.DRAFT) {
        throw new BusinessRuleException("Không thể chuyển về DRAFT.");
    }

    if (from == ItemStatus.SERVED) {
        throw new BusinessRuleException("Món đã SERVED, không thể đổi trạng thái.");
    }
    if (from == ItemStatus.CANCELED) {
        throw new BusinessRuleException("Món đã CANCELED, không thể đổi trạng thái.");
    }

    boolean ok =
            (from == ItemStatus.DRAFT   && to == ItemStatus.PENDING) ||
            (from == ItemStatus.PENDING && (to == ItemStatus.COOKING || to == ItemStatus.CANCELED)) ||
            (from == ItemStatus.COOKING && (to == ItemStatus.READY   || to == ItemStatus.CANCELED)) ||
            (from == ItemStatus.READY   && to == ItemStatus.SERVED);

    if (!ok) {
        throw new BusinessRuleException("Chuyển trạng thái không hợp lệ: " + from + " -> " + to);
    }

    if (to == ItemStatus.CANCELED) {
    
    }
}
public BillDto getBill(Long orderId) {
    OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy order id=" + orderId));

    List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByIdAsc(orderId);

    // Tính bill trên các món không bị CANCELED
    List<BillDto.BillItemDto> billItems = items.stream()
            .filter(i -> i.getStatus() != ItemStatus.CANCELED)
            .map(i -> {
                BigDecimal lineTotal = i.getUnitPriceSnapshot()
                        .multiply(BigDecimal.valueOf(i.getQty()))
                        .setScale(2, RoundingMode.HALF_UP);
                return new BillDto.BillItemDto(
                        i.getId(),
                        i.getItemNameSnapshot(),
                        i.getUnitPriceSnapshot(),
                        i.getQty(),
                        lineTotal,
                        i.getStatus()
                );
            })
            .toList();

    BigDecimal subtotal = billItems.stream()
            .map(BillDto.BillItemDto::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

    // MVP: mặc định 0 (bill endpoint chỉ hiển thị subtotal)
    BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    BigDecimal tax = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    BigDecimal serviceFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    BigDecimal total = subtotal.subtract(discount).add(tax).add(serviceFee)
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);

    return new BillDto(
            order.getId(),
            order.getTableId(),
            order.getStatus(),
            billItems,
            subtotal,
            discount,
            tax,
            serviceFee,
            total
    );
}

@Transactional
public CheckoutResponse checkout(Long orderId, CheckoutRequest req) {
    OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy order id=" + orderId));

    if (order.getStatus() != OrderStatus.ACTIVE) {
        throw new BusinessRuleException("Chỉ checkout được order ACTIVE. Hiện tại: " + order.getStatus());
    }

    // Chặn checkout 2 lần
    if (paymentRepository.findByOrderId(orderId).isPresent()) {
        throw new BusinessRuleException("Order đã được thanh toán trước đó.");
    }

    // Tính subtotal từ snapshot price
    List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByIdAsc(orderId)
            .stream()
            .filter(i -> i.getStatus() != ItemStatus.CANCELED)
            .toList();

    if (items.isEmpty()) {
        throw new BusinessRuleException("Order không có món hợp lệ để thanh toán.");
    }

    BigDecimal subtotal = items.stream()
            .map(i -> i.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(i.getQty())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal discount = (req.discountAmount() == null ? BigDecimal.ZERO : req.discountAmount()).setScale(2, RoundingMode.HALF_UP);
    BigDecimal tax = (req.taxAmount() == null ? BigDecimal.ZERO : req.taxAmount()).setScale(2, RoundingMode.HALF_UP);
    BigDecimal serviceFee = (req.serviceFeeAmount() == null ? BigDecimal.ZERO : req.serviceFeeAmount()).setScale(2, RoundingMode.HALF_UP);

    if (discount.compareTo(subtotal) > 0) {
        throw new BusinessRuleException("Discount không được lớn hơn subtotal.");
    }

    BigDecimal total = subtotal.subtract(discount).add(tax).add(serviceFee)
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);

    PaymentEntity payment = new PaymentEntity();
    payment.setOrderId(orderId);
    payment.setSubtotal(subtotal);
    payment.setDiscountAmount(discount);
    payment.setTaxAmount(tax);
    payment.setServiceFeeAmount(serviceFee);
    payment.setTotalAmount(total);
    payment.setMethod(req.method());
    payment.setPaidAt(LocalDateTime.now());

    PaymentEntity saved = paymentRepository.save(payment);

    // Đóng order
    order.setStatus(OrderStatus.COMPLETED);
    order.setCompletedAt(LocalDateTime.now());
    orderRepository.save(order);

    // Update bàn -> CLEANING + clear current_order_id
    TableEntity table = tableRepository.findById(order.getTableId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn của order."));

    table.setStatus(TableStatus.CLEANING);
    table.setCurrentOrderId(null);
    tableRepository.save(table);

    return new CheckoutResponse(saved.getId(), orderId, total, "Thanh toán thành công. Bàn chuyển sang CLEANING.");
}


}
