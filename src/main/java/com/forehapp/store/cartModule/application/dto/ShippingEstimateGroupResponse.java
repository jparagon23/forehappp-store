package com.forehapp.store.cartModule.application.dto;

import java.math.BigDecimal;

public record ShippingEstimateGroupResponse(
        Long storeId,
        String storeName,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        boolean freeShipping
) {}
