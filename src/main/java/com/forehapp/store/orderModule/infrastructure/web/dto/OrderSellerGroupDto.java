package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderSellerGroupDto(
        Long groupId,
        Long storeId,
        String storeName,
        String status,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        String trackingNumber,
        LocalDateTime preparedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        List<OrderItemDto> items
) {}
