package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SellerOrderGroupDto(
        Long groupId,
        Long orderId,
        String buyerName,
        String shippingAddress,
        String shippingCity,
        String shippingCountry,
        String status,
        BigDecimal subtotal,
        String trackingNumber,
        LocalDateTime preparedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt,
        String cancellationReason,
        List<OrderItemDto> items
) {}
