package com.forehapp.store.supplierSyncModule.domain.model;

public enum SyncEventType {
    /** Supplier is out of stock: variant stock set to 0. */
    DISABLED,
    /** Supplier has it again: stock restored on a variant the sync had disabled. */
    REENABLED,
    /** Seller restocked a sync-disabled variant by hand: the sync stops tracking it. */
    RELEASED,
    COST_UPDATED,
    /** Confirmed pair whose supplier product no longer appears in the catalog. */
    BROKEN_LINK,
    MARGIN_ALERT,
    /** Open order containing a variant that was just disabled. */
    ORDER_AT_RISK
}
