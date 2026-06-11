package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemDto(
        Long itemId,
        Long variantId,
        String sku,
        String productTitle,
        String categoryName,
        String brandName,
        String lineName,
        List<VariantAttributeDto> attributes,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
