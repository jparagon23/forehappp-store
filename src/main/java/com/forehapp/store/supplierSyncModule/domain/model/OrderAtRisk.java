package com.forehapp.store.supplierSyncModule.domain.model;

/** An open (not yet shipped) order line for a variant the sync just disabled. */
public record OrderAtRisk(Long orderId, Long variantId, Integer quantity) {}
