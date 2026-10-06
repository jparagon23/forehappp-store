package com.forehapp.store.supplierSyncModule.domain.model;

import java.math.BigDecimal;

/** A supplier product as seen by the current run; seenInRun is false when it was missing from the latest catalog. */
public record ItemSnapshot(Long id, String name, BigDecimal price, boolean outOfStock, boolean seenInRun) {}
