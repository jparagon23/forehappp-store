package com.forehapp.store.catalogRequestModule.application.dto;

import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCatalogRequestDto(
        @NotNull(message = "Type is required")
        CatalogRequestType type,

        @NotBlank(message = "Suggested name is required")
        @Size(min = 2, max = 100, message = "Suggested name must be between 2 and 100 characters")
        String suggestedName
) {}
