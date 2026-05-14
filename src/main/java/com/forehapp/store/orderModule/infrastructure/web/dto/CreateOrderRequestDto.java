package com.forehapp.store.orderModule.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequestDto(
        @NotNull(message = "Address ID is required") Long addressId
) {}
