package com.forehapp.store.catalogRequestModule.application.dto;

import jakarta.validation.constraints.Size;

public record RejectCatalogRequestDto(
        @Size(max = 255, message = "Rejection reason cannot exceed 255 characters")
        String reason
) {}
