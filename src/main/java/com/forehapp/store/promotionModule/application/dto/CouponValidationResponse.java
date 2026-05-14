package com.forehapp.store.promotionModule.application.dto;

import java.math.BigDecimal;

public record CouponValidationResponse(
        boolean valid,
        Long couponId,
        String code,
        String discountType,
        BigDecimal discountValue,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        String message
) {}
