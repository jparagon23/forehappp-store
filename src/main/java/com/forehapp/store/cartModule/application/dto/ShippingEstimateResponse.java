package com.forehapp.store.cartModule.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ShippingEstimateResponse(
        List<ShippingEstimateGroupResponse> sellerGroups,
        BigDecimal itemsTotal,       // sum of all items, before shipping
        BigDecimal shippingTotal,    // sum of all shipping costs
        BigDecimal grandTotal,       // itemsTotal + shippingTotal (base total, no MP fee)
        BigDecimal mercadoPagoSurcharge,  // 3.5% of grandTotal; show only when MP is selected
        BigDecimal mercadoPagoGrandTotal  // grandTotal + mercadoPagoSurcharge
) {}
