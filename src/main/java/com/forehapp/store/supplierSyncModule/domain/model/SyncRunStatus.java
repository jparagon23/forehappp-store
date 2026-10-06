package com.forehapp.store.supplierSyncModule.domain.model;

public enum SyncRunStatus {
    PREVIEW,
    APPLIED,
    /** The safety checks rejected the data; nothing was changed. */
    ABORTED
}
