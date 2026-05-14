package com.forehapp.store.promotionModule.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CouponResponse(
        Long couponId,
        Long sellerId,
        String sellerName,
        String code,
        String description,
        String discountType,
        BigDecimal discountValue,
        BigDecimal minOrderAmount,
        Integer maxUses,
        Integer usesCount,
        Integer maxUsesPerUser,
        LocalDate validFrom,
        LocalDate validUntil,
        String status,
        LocalDateTime createdAt
) {}
