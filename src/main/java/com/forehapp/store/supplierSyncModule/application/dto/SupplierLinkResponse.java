package com.forehapp.store.supplierSyncModule.application.dto;

import java.math.BigDecimal;

/**
 * One row of the seller pairing screen. linkId and status are null for variants without any pair yet.
 * marginPercent compares the variant price with the supplier price.
 */
public record SupplierLinkResponse(
        Long linkId,
        String status,
        BigDecimal score,
        boolean disabledBySync,
        Variant variant,
        SupplierItemResponse supplierItem,
        BigDecimal marginPercent
) {
    public record Variant(
            Long variantId,
            Long productId,
            String productTitle,
            String brand,
            String sku,
            String attributes,
            boolean active,
            int stock,
            BigDecimal price
    ) {}
}
