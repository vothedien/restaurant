package com.restaurant.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.dto.ActionResponse;
import com.restaurant.dto.AddOrderItemRequest;
import com.restaurant.dto.BillDto;
import com.restaurant.dto.CheckoutRequest;
import com.restaurant.dto.CheckoutResponse;
import com.restaurant.dto.DraftOrderDto;
import com.restaurant.dto.OrderDetailDto;
import com.restaurant.dto.UpdateItemStatusRequest;
import com.restaurant.dto.UpdateOrderItemRequest;
import com.restaurant.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/draft")
    public DraftOrderDto getDraft(@RequestParam Long tableId) {
        return orderService.getDraftOrderByTable(tableId);
    }

    @PostMapping("/{orderId}/confirm")
    public ActionResponse confirm(@PathVariable Long orderId) {
        return orderService.confirmOrder(orderId);
    }
    @PostMapping("/{orderId}/items")
    public ActionResponse addItem(@PathVariable Long orderId, @Valid @RequestBody AddOrderItemRequest req) {
        return orderService.addItem(orderId, req);
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public ActionResponse updateItem(@PathVariable Long orderId, @PathVariable Long itemId,
                                 @Valid @RequestBody UpdateOrderItemRequest req) {
        return orderService.updateItem(orderId, itemId, req);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ActionResponse removeItem(@PathVariable Long orderId, @PathVariable Long itemId) {
        return orderService.removeItem(orderId, itemId);
    }
    @GetMapping("/{orderId}")
    public OrderDetailDto detail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(orderId);
    }
    @PostMapping("/{orderId}/items/{itemId}/status")
    public ActionResponse updateItemStatus(@PathVariable Long orderId,
                                       @PathVariable Long itemId,
                                       @Valid @RequestBody UpdateItemStatusRequest req) {
        return orderService.updateItemStatus(orderId, itemId, req);
    } 
    @GetMapping("/{orderId}/bill")
    public BillDto bill(@PathVariable Long orderId) {
        return orderService.getBill(orderId);
    }

    @PostMapping("/{orderId}/checkout")
    public CheckoutResponse checkout(@PathVariable Long orderId, @Valid @RequestBody CheckoutRequest req) {
        return orderService.checkout(orderId, req);
    }


}
