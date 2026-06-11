package com.forehapp.store.cartModule.application.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long itemId,
        Long variantId,
        Long productId,
        String sku,
        String productTitle,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        boolean priceChanged,
        BigDecimal previousPrice,
        String thumbnailUrl
) {}
