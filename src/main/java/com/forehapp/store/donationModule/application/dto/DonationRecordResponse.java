package com.forehapp.store.donationModule.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DonationRecordResponse(
        Long id,
        Long foundationId,
        String foundationName,
        Long orderId,
        String couponCode,
        Long donorProfileId,
        String donorEmail,
        BigDecimal donationAmount,
        BigDecimal donationPercentage,
        String status,
        LocalDateTime createdAt
) {}
