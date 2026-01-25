package com.restaurant.service.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.dto.admin.AdminTableDto;
import com.restaurant.dto.admin.CreateTableRequest;
import com.restaurant.dto.admin.UpdateTableRequest;
import com.restaurant.entity.TableEntity;
import com.restaurant.enums.TableStatus;
import com.restaurant.exception.BusinessRuleException;
import com.restaurant.exception.NotFoundException;
import com.restaurant.repository.TableRepository;

@Service
public class AdminTableService {

    private final TableRepository tableRepository;

    public AdminTableService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public List<AdminTableDto> list() {
        return tableRepository.findAll().stream()
                .map(t -> new AdminTableDto(t.getId(), t.getCode(), t.getCapacity(), t.getStatus(), t.getQrToken()))
                .toList();
    }

    @Transactional
    public AdminTableDto create(CreateTableRequest req) {
        String code = req.code().trim();
        if (tableRepository.existsByCode(code)) {
            throw new BusinessRuleException("Code bàn đã tồn tại: " + code);
        }

        String token = (req.qrToken() != null && !req.qrToken().isBlank())
                ? req.qrToken().trim()
                : generateUniqueToken();

        if (tableRepository.existsByQrToken(token)) {
            throw new BusinessRuleException("qrToken đã tồn tại: " + token);
        }

        TableEntity t = new TableEntity();
        t.setCode(code);
        t.setCapacity(req.capacity() == null ? 1 : req.capacity());
        t.setStatus(TableStatus.AVAILABLE);
        t.setQrToken(token);
        t.setCurrentOrderId(null);

        TableEntity saved = tableRepository.save(t);
        return new AdminTableDto(saved.getId(), saved.getCode(), saved.getCapacity(), saved.getStatus(), saved.getQrToken());
    }

    @Transactional
    public AdminTableDto update(Long id, UpdateTableRequest req) {
        TableEntity t = tableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn id=" + id));

        String newCode = req.code().trim();
        if (!newCode.equalsIgnoreCase(t.getCode()) && tableRepository.existsByCode(newCode)) {
            throw new BusinessRuleException("Code bàn đã tồn tại: " + newCode);
        }

        t.setCode(newCode);
        t.setCapacity(req.capacity());
        t.setStatus(req.status());

        TableEntity saved = tableRepository.save(t);
        return new AdminTableDto(saved.getId(), saved.getCode(), saved.getCapacity(), saved.getStatus(), saved.getQrToken());
    }

    private String generateUniqueToken() {
     
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        } while (tableRepository.existsByQrToken(token));
        return token;
    }
    @Transactional
    public void delete(Long id) {
    TableEntity t = tableRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn id=" + id));
    try {
        tableRepository.delete(t);
    } catch (DataIntegrityViolationException ex) {
        throw new BusinessRuleException("Không thể xoá bàn vì đã có order liên quan. (Gợi ý: giữ bàn, set AVAILABLE)");
    }
}
}
