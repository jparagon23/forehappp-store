package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderSellerGroupDto(
        Long groupId,
        Long sellerId,
        String sellerName,
        String status,
        BigDecimal subtotal,
        List<OrderItemDto> items
) {}
