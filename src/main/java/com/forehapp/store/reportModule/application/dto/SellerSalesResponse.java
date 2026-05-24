package com.forehapp.store.reportModule.application.dto;

import java.math.BigDecimal;

public record SellerSalesResponse(
        Long storeId,
        String storeName,
        Long totalOrders,
        BigDecimal totalRevenue
) {}
