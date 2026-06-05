package com.forehapp.store.cartModule.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartSellerGroupResponse(
        Long storeId,
        String storeName,
        List<CartItemResponse> items,
        BigDecimal subtotal,
        BigDecimal freeShippingMinAmount
) {}
