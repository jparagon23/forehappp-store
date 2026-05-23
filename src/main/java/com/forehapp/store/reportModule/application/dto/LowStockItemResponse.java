package com.forehapp.store.reportModule.application.dto;

public record LowStockItemResponse(
        Long variantId,
        Long productId,
        String productTitle,
        String sku,
        Integer stock
) {}
