package com.forehapp.store.cartModule.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartSellerGroupResponse(
        Long sellerId,
        String sellerName,
        List<CartItemResponse> items,
        BigDecimal subtotal
) {}
