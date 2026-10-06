package com.forehapp.store.supplierSyncModule.application.dto;

import com.forehapp.store.supplierSyncModule.domain.model.SyncMode;
import jakarta.validation.constraints.NotNull;

public record SyncConfigDto(
        String supplier,
        @NotNull(message = "Enabled is required") Boolean enabled,
        @NotNull(message = "Mode is required") SyncMode mode
) {}
