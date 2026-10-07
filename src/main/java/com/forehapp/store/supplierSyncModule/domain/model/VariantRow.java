package com.forehapp.store.supplierSyncModule.domain.model;

import java.math.BigDecimal;

/** A store variant as shown in the pairing screens and sent to the matching script. */
public record VariantRow(
        Long variantId,
        Long productId,
        Long storeId,
        String productTitle,
        String brand,
        String sku,
        Boolean active,
        Integer stock,
        Boolean dropship,
        Boolean supplierAvailable,
        BigDecimal price
) {}
