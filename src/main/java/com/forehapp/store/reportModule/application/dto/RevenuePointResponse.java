package com.forehapp.store.reportModule.application.dto;

import java.math.BigDecimal;

public record RevenuePointResponse(
        String period,
        Long orderCount,
        BigDecimal revenue
) {}
