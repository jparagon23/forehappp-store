package com.forehapp.store.supplierSyncModule.domain.model;

public enum SyncMode {
    /** Computes and reports what would change, without touching stock or costs. */
    PREVIEW,
    APPLY
}
