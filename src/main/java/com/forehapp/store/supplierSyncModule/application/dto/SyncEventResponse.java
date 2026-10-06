package com.forehapp.store.supplierSyncModule.application.dto;

import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncEvent;

public record SyncEventResponse(
        Long id,
        String type,
        Long variantId,
        Long productId,
        String productTitle,
        String variantLabel,
        String supplierItemName,
        String oldValue,
        String newValue,
        String detail,
        boolean unconfirmed
) {
    public static SyncEventResponse from(SupplierSyncEvent e) {
        return new SyncEventResponse(e.getId(), e.getType().name(), e.getVariantId(), e.getProductId(),
                e.getProductTitle(), e.getVariantLabel(), e.getSupplierItemName(), e.getOldValue(),
                e.getNewValue(), e.getDetail(), Boolean.TRUE.equals(e.getUnconfirmed()));
    }
}
