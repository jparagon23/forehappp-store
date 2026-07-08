package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SellerOrderGroupDto(
        Long groupId,
        Long orderId,
        String buyerName,
        String buyerPhone,
        String buyerEmail,
        String shippingAddress,
        String shippingCity,
        String shippingCountry,
        String paymentMethod,
        String orderPaymentStatus,
        String status,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        BigDecimal orderTotal,
        String couponCode,
        BigDecimal couponDiscount,
        String trackingNumber,
        LocalDateTime preparedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt,
        String cancellationReason,
        BigDecimal shippingCostWaived,
        LocalDateTime shippingRemovedAt,
        String shippingRemovedReason,
        List<OrderItemDto> items,
        BigDecimal totalCost,
        BigDecimal totalMargin,
        BigDecimal marginPercent
) {}
