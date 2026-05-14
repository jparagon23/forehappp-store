package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDto(
        Long orderId,
        String status,
        BigDecimal total,
        LocalDateTime createdAt,
        int sellerGroupCount
) {}
