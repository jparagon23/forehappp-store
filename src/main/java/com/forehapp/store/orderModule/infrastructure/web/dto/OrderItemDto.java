package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        Long itemId,
        Long variantId,
        String sku,
        String productTitle,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
