package com.forehapp.store.supplierSyncModule.application.dto;

import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncRun;

import java.time.LocalDateTime;

public record SyncRunResponse(
        Long id,
        Long storeId,
        String supplier,
        String mode,
        String status,
        String abortReason,
        int supplierItems,
        int supplierOutOfStock,
        int confirmedLinks,
        int disabled,
        int reenabled,
        int costUpdates,
        int brokenLinks,
        int marginAlerts,
        int ordersAtRisk,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
    public static SyncRunResponse from(SupplierSyncRun run) {
        return new SyncRunResponse(run.getId(), run.getStoreId(), run.getSupplier().name(), run.getMode().name(),
                run.getStatus().name(), run.getAbortReason(), run.getSupplierItems(), run.getSupplierOutOfStock(),
                run.getConfirmedLinks(), run.getDisabledCount(), run.getReenabledCount(), run.getCostUpdates(),
                run.getBrokenLinks(), run.getMarginAlerts(), run.getOrdersAtRisk(), run.getStartedAt(), run.getFinishedAt());
    }
}
