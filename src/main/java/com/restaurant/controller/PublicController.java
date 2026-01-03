package com.restaurant.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.dto.PublicMenuItemDto;
import com.restaurant.dto.PublicTableInfoDto;
import com.restaurant.dto.SubmitOrderRequest;
import com.restaurant.dto.SubmitOrderResponse;
import com.restaurant.entity.TableEntity;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.TableRepository;
import com.restaurant.service.PublicOrderingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final TableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    private final PublicOrderingService publicOrderingService;

    public PublicController(TableRepository tableRepository,
                            MenuItemRepository menuItemRepository,
                            PublicOrderingService publicOrderingService) {
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
        this.publicOrderingService = publicOrderingService;
    }

    // Khách mở link -> lấy thông tin bàn
    @GetMapping("/tables/{token}")
    public PublicTableInfoDto getTableByToken(@PathVariable String token) {
        TableEntity t = tableRepository.findByQrToken(token)
                .orElseThrow(() -> new com.restaurant.exception.NotFoundException("Không tìm thấy bàn với token=" + token));
        return new PublicTableInfoDto(t.getId(), t.getCode(), t.getStatus());
    }

    // Menu public (có imageUrl)
    @GetMapping("/menu")
    public List<PublicMenuItemDto> getMenu() {
        return menuItemRepository.findAll().stream()
                .map(m -> new PublicMenuItemDto(
                        m.getId(),
                        m.getCategoryId(),
                        m.getName(),
                        m.getPrice(),
                        m.getIsAvailable(),
                        m.getImageUrl()   // bạn đang dùng image_url
                ))
                .toList();
    }

    // Khách submit order DRAFT theo token
    @PostMapping("/tables/{token}/submit")
    public SubmitOrderResponse submit(@PathVariable String token, @Valid @RequestBody SubmitOrderRequest req) {
        return publicOrderingService.submitByToken(token, req);
    }
}
