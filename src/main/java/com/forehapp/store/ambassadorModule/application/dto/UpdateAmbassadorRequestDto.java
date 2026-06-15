package com.forehapp.store.ambassadorModule.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateAmbassadorRequestDto(
        @DecimalMin(value = "0.01", message = "Commission percentage must be greater than 0")
        @DecimalMax(value = "100.00", message = "Commission percentage cannot exceed 100")
        BigDecimal commissionPercentage,
        String status
) {}
