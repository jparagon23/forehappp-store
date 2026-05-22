package com.forehapp.store.shippingModule.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateShippingZoneDto(
        @NotBlank @Size(max = 150) String name,
        List<String> cities,
        @NotNull @DecimalMin("0") BigDecimal cost,
        Boolean isDefault
) {}
