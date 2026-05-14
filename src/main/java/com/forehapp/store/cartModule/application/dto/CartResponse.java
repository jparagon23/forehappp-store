package com.forehapp.store.cartModule.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        Long cartId,
        String status,
        LocalDateTime updatedAt,
        BigDecimal total,
        List<CartSellerGroupResponse> sellerGroups
) {}
