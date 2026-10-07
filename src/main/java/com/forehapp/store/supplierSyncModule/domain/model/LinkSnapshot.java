package com.forehapp.store.supplierSyncModule.domain.model;

import java.math.BigDecimal;

/** A supplier link joined with the current state of its variant, as read at the start of a run. */
public record LinkSnapshot(
        Long linkId,
        Long variantId,
        Long productId,
        String productTitle,
        String variantLabel,
        Boolean variantActive,
        Integer variantStock,
        Boolean variantDropship,
        Boolean variantSupplierAvailable,
        BigDecimal variantPrice,
        BigDecimal variantCost,
        SupplierLinkStatus status,
        Long supplierItemId
) {}
