package com.forehapp.store.promotionModule.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDonationCouponRequestDto(
        @NotBlank @Size(max = 50) String code,
        @Size(max = 255) String description,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal discountValue,
        @NotNull Long foundationId,
        @DecimalMin("0.0") BigDecimal minOrderAmount,
        @Min(1) Integer maxUses,
        @NotNull @Min(1) Integer maxUsesPerUser,
        @NotNull LocalDate validFrom,
        LocalDate validUntil
) {}
