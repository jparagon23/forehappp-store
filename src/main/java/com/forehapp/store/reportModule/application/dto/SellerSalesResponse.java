package com.forehapp.store.reportModule.application.dto;

import java.math.BigDecimal;

public record SellerSalesResponse(
        Long sellerId,
        String sellerName,
        Long totalOrders,
        BigDecimal totalRevenue
) {}
