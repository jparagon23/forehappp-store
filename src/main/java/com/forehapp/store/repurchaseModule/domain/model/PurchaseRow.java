package com.forehapp.store.repurchaseModule.domain.model;

import com.forehapp.store.productModule.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One purchased order item of a product that has a repurchase duration configured. */
public record PurchaseRow(
        Long itemId,
        Long orderId,
        LocalDateTime orderCreatedAt,
        String email,
        String guestName,
        String buyerName,
        Integer quantity,
        Long variantId,
        Integer variantRepurchaseDays,
        Boolean variantActive,
        // Own stock or available from the supplier (dropship)
        Boolean variantInStock,
        BigDecimal variantPrice,
        Long productId,
        String productTitle,
        Integer productRepurchaseDays,
        ProductStatus productStatus,
        LocalDateTime deliveredAt,
        LocalDateTime shippedAt
) {}
