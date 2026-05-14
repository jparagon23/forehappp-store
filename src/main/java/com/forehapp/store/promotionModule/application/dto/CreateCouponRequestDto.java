package com.forehapp.store.promotionModule.application.dto;

import com.forehapp.store.promotionModule.domain.model.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCouponRequestDto(
        @NotBlank @Size(max = 50) String code,
        @Size(max = 255) String description,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        @DecimalMin("0.0") BigDecimal minOrderAmount,
        @Min(1) Integer maxUses,
        @NotNull @Min(1) Integer maxUsesPerUser,
        @NotNull LocalDate validFrom,
        LocalDate validUntil
) {}
