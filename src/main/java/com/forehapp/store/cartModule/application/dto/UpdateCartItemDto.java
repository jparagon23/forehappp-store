package com.forehapp.store.cartModule.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemDto(
        @NotNull @Min(1) Integer quantity
) {}
