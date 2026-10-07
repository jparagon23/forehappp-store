package com.forehapp.store.supplierSyncModule.domain.model;

public enum SyncEventType {
    /** Supplier ran out of a dropship variant: only its own stock can be sold now. newValue = own stock. */
    DISABLED,
    /** Supplier has a dropship variant again: it can be sold without own stock. newValue = own stock. */
    REENABLED,
    /** No longer produced (from when the sync changed stock); kept to read old runs. */
    RELEASED,
    COST_UPDATED,
    /** Confirmed pair whose supplier product no longer appears in the catalog. */
    BROKEN_LINK,
    MARGIN_ALERT,
    /** Open order with units to order from a supplier that just ran out. newValue = those units. */
    ORDER_AT_RISK
}
