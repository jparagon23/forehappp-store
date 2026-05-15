package com.forehapp.store.reportModule.application.dto;

import java.math.BigDecimal;

public record TopProductResponse(
        Long productId,
        String productTitle,
        String variantSku,
        Long unitsSold,
        BigDecimal revenue
) {}
