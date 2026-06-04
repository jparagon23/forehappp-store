package com.forehapp.store.orderModule.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GuestOrderItemDto(
        @NotNull(message = "Variant ID is required") Long variantId,
        @NotNull @Min(value = 1, message = "Quantity must be at least 1") Integer quantity
) {}
