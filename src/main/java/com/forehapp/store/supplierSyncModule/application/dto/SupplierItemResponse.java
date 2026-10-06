package com.forehapp.store.supplierSyncModule.application.dto;

import com.forehapp.store.supplierSyncModule.domain.model.SupplierCatalogItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SupplierItemResponse(
        Long id,
        String name,
        String brand,
        String category,
        BigDecimal price,
        boolean outOfStock,
        LocalDateTime lastSeenAt
) {
    public static SupplierItemResponse from(SupplierCatalogItem item) {
        return new SupplierItemResponse(item.getId(), item.getName(), item.getBrand(), item.getCategory(),
                item.getPrice(), Boolean.TRUE.equals(item.getOutOfStock()), item.getLastSeenAt());
    }
}
