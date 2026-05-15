package com.forehapp.store.cartModule.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddItemRequestDto(
        @NotNull Long variantId,
        @NotNull @Min(1) @Max(9999) Integer quantity
) {}
