package com.forehapp.store.orderModule.domain.model;

public enum OrderChannel {
    /** Placed by the buyer in the store (logged in or as guest). */
    ONLINE,
    /** Registered by a seller on behalf of a customer who ordered outside the app (e.g. WhatsApp). */
    ASSISTED
}
