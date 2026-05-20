package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDto(
        Long orderId,
        String paymentStatus,
        String paymentMethod,
        String shippingStatus,
        BigDecimal total,
        LocalDateTime createdAt,
        int sellerGroupCount
) {}
