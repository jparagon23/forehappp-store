package com.forehapp.store.returnModule.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReturnResponse(
        Long returnId,
        Long orderGroupId,
        Long orderId,
        String buyerName,
        String returnType,
        String reason,
        BigDecimal refundAmount,
        String adminNotes,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ReturnItemResponse> items
) {}
