package com.forehapp.store.orderModule.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ShipGroupRequestDto(
        @NotBlank String trackingNumber
) {}
