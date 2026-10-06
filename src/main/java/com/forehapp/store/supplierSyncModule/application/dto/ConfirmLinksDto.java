package com.forehapp.store.supplierSyncModule.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ConfirmLinksDto(
        @NotEmpty(message = "At least one link is required")
        @Size(max = 500, message = "At most 500 links per request")
        List<Long> linkIds
) {}
