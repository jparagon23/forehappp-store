package com.forehapp.store.promotionModule.application.dto;

import com.forehapp.store.promotionModule.domain.model.PromotionStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCouponRequestDto(
        @Size(max = 255) String description,
        @DecimalMin("0.0") BigDecimal minOrderAmount,
        @Min(1) Integer maxUses,
        LocalDate validUntil,
        PromotionStatus status
) {}
