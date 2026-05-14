package com.forehapp.store.returnModule.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReturnItemRequestDto(
        @NotNull Long orderItemId,
        @NotNull @Min(1) Integer quantityToReturn
) {}
