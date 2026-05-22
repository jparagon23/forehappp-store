package com.forehapp.store.cartModule.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ShippingEstimateResponse(
        List<ShippingEstimateGroupResponse> sellerGroups,
        BigDecimal itemsTotal,
        BigDecimal shippingTotal,
        BigDecimal grandTotal
) {}
