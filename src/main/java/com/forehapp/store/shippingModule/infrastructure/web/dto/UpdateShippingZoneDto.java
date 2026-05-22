package com.forehapp.store.shippingModule.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateShippingZoneDto(
        @Size(min = 1, max = 150) String name,
        List<String> cities,
        @DecimalMin("0") BigDecimal cost,
        Boolean isDefault,
        Boolean active
) {}
