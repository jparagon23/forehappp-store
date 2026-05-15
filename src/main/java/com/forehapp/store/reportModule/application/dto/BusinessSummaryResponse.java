package com.forehapp.store.reportModule.application.dto;

import java.math.BigDecimal;

public record BusinessSummaryResponse(
        Long totalOrders,
        BigDecimal totalRevenue,
        BigDecimal averageTicket,
        Long cancelledOrders,
        Long totalReturns,
        BigDecimal totalRefunded
) {}
