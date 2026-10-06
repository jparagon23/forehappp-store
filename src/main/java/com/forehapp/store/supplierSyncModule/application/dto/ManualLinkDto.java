package com.forehapp.store.supplierSyncModule.application.dto;

import jakarta.validation.constraints.NotNull;

public record ManualLinkDto(@NotNull(message = "Supplier item is required") Long supplierItemId) {}
