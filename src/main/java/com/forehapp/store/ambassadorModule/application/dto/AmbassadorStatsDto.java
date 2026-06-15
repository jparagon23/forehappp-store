package com.forehapp.store.ambassadorModule.application.dto;

import java.math.BigDecimal;

public record AmbassadorStatsDto(
        String referralCode,
        BigDecimal commissionPercentage,
        long totalOrders,
        BigDecimal totalEarned,
        BigDecimal pendingAmount,
        BigDecimal paidAmount
) {}
