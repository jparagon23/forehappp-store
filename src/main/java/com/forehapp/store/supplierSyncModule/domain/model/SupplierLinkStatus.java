package com.forehapp.store.supplierSyncModule.domain.model;

public enum SupplierLinkStatus {
    /** Proposed by the matching script; only used for margin alerts, never to change stock or cost. */
    SUGGESTED,
    /** Confirmed by the seller; drives stock and cost sync. */
    CONFIRMED,
    /** Seller rejected this pair; it is never suggested again. */
    REJECTED,
    /** Seller marked the variant as not sourced from this supplier. */
    NOT_SUPPLIED
}
