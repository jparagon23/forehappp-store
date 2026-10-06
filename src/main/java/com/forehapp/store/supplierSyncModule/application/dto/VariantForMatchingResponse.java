package com.forehapp.store.supplierSyncModule.application.dto;

import java.math.BigDecimal;
import java.util.List;

/** A store variant still without a confirmed pair, sent to the matching script. */
public record VariantForMatchingResponse(
        Long variantId,
        Long productId,
        Long storeId,
        String title,
        String brand,
        String sku,
        BigDecimal price,
        boolean active,
        List<Attribute> attributes
) {
    public record Attribute(String attribute, String value) {}
}
