package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String status,
        BigDecimal total,
        String shippingAddress,
        String shippingCity,
        String shippingCountry,
        LocalDateTime createdAt,
        List<OrderSellerGroupDto> sellerGroups,
        String checkoutUrl
) {}
