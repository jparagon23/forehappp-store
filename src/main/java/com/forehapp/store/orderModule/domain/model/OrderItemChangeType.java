package com.forehapp.store.orderModule.domain.model;

public enum OrderItemChangeType {
    /** Product swapped for another one (the line keeps its place in the order). */
    REPLACED,
    /** Same product, different quantity and/or price. */
    UPDATED,
    ADDED,
    REMOVED
}
