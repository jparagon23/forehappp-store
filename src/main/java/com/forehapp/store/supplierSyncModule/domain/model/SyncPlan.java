package com.forehapp.store.supplierSyncModule.domain.model;

import java.math.BigDecimal;
import java.util.List;

/** What a run decided to do. Actions are only executed in APPLY mode and when abortReason is null. */
public record SyncPlan(List<Action> actions, List<EventDraft> events, String abortReason) {

    public enum ActionType { DISABLE, REENABLE, RELEASE, UPDATE_COST }

    /**
     * stock: for DISABLE the stock to remember, for REENABLE the stock to restore.
     * cost: for UPDATE_COST the new cost.
     */
    public record Action(ActionType type, Long linkId, Long variantId, Long productId, Integer stock, BigDecimal cost) {}

    public record EventDraft(
            SyncEventType type,
            Long variantId,
            Long productId,
            String productTitle,
            String variantLabel,
            String supplierItemName,
            String oldValue,
            String newValue,
            String detail,
            boolean unconfirmed
    ) {}

    public long count(SyncEventType type) {
        return events.stream().filter(e -> e.type() == type).count();
    }

    public boolean aborted() {
        return abortReason != null;
    }
}
