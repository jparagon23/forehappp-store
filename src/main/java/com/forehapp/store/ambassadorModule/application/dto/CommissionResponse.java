package com.forehapp.store.ambassadorModule.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CommissionResponse(
        Long commissionId,
        Long orderId,
        BigDecimal commissionAmount,
        BigDecimal commissionPercentage,
        String status,
        LocalDateTime createdAt
) {}
