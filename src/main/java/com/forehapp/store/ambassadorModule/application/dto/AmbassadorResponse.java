package com.forehapp.store.ambassadorModule.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AmbassadorResponse(
        Long ambassadorId,
        Long profileId,
        String userName,
        String userEmail,
        String referralCode,
        BigDecimal commissionPercentage,
        String status,
        LocalDateTime createdAt,
        AmbassadorStatsDto stats
) {}
