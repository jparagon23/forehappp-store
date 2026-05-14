package com.forehapp.store.promotionModule.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ValidateCouponRequestDto(
        @NotBlank String code,
        @NotNull @DecimalMin("0.01") BigDecimal orderAmount
) {}
