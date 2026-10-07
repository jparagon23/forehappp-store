package com.forehapp.store.supplierSyncModule.application.dto;

import java.math.BigDecimal;

/**
 * One row of the seller pairing screen. linkId and status are null for variants without any pair yet.
 * marginPercent compares the variant price with the supplier price.
 * disabledBySync: dropship variant the supplier is out of (only its own stock sells).
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
            boolean dropship,
            boolean supplierAvailable,
            BigDecimal price
    ) {}
}
