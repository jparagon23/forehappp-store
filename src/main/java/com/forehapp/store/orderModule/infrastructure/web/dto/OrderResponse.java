package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String paymentStatus,
        String paymentMethod,
        // --- price breakdown (all amounts in COP) ---
        BigDecimal subtotal,           // sum of all items, before shipping/discounts/surcharge
        BigDecimal shippingTotal,      // sum of all seller-group shipping costs
        String couponCode,             // null if no coupon was applied
        BigDecimal couponDiscount,     // null if no coupon was applied
        BigDecimal mercadoPagoSurcharge, // 3.5% fee; null for non-MP payment methods
        BigDecimal total,              // final amount: subtotal + shippingTotal - couponDiscount + mercadoPagoSurcharge
        // -------------------------------------------
        String shippingAddress,
        String shippingCity,
        String shippingCountry,
        LocalDateTime createdAt,
        List<OrderSellerGroupDto> sellerGroups,
        String checkoutUrl
) {}
