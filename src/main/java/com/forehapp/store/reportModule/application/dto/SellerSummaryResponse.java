package com.forehapp.store.reportModule.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record SellerSummaryResponse(
        Long totalOrders,
        BigDecimal totalRevenue,
        BigDecimal averageTicket,
        Long pendingToShip,
        Long inTransit,
        Long deliveredOrders,
        Long cancelledOrders,
        Long totalReturns,
        BigDecimal totalRefunded,
        Long lowStockCount,
        List<LowStockItemResponse> lowStockItems
) {}
