package com.forehapp.store.supplierSyncModule.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Payload sent daily by the scraping script: the full supplier catalog plus pairing suggestions. */
public record SupplierCatalogIngestDto(
        @NotNull(message = "Items are required") @Valid List<Item> items,
        @Valid List<Suggestion> suggestions
) {

    public record Item(
            @NotBlank(message = "Item name is required") @Size(max = 255) String name,
            @Size(max = 150) String brand,
            @Size(max = 150) String category,
            BigDecimal price,
            boolean outOfStock
    ) {}

    /** Best supplier product found by the matching script for a store variant. */
    public record Suggestion(
            @NotNull(message = "Variant id is required") Long variantId,
            @NotBlank(message = "Supplier name is required") String supplierName,
            BigDecimal score
    ) {}
}
