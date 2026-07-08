package com.forehapp.store.orderModule.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RemoveShippingCostRequestDto(
        @NotBlank @Size(max = 500) String reason
) {}
