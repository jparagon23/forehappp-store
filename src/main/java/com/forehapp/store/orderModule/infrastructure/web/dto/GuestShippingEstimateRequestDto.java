package com.forehapp.store.orderModule.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GuestShippingEstimateRequestDto(
        @NotNull(message = "City is required") Long cityId,
        @NotEmpty(message = "Items list cannot be empty")
        @Size(max = 50)
        @Valid List<GuestOrderItemDto> items
) {}
