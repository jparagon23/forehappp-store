package com.forehapp.store.reportModule.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin home. "Sales" are orders that count as sold: not cancelled and either paid (Mercado Pago or a
 * confirmed transfer/cash payment) or cash on delivery. Each KPI carries the previous period of the same
 * length for comparison.
 */
public record AdminDashboardResponse(
        Period period,
        Kpis kpis,
        Operations operations,
        Catalog catalog,
        Reviews reviews,
        List<DayPoint> series,
        List<TopProduct> topProducts,
        List<RecentOrder> recentOrders,
        SupplierSync supplierSync
) {
    public record Period(LocalDate from, LocalDate to, int days) {}

    public record Metric(BigDecimal value, BigDecimal previous) {}

    /** profit: sale price − cost on sold lines that have a cost; itemsWithoutCost tells how complete it is. */
    public record Kpis(Metric revenue, Metric orders, Metric averageTicket, Metric profit,
                       long soldItems, long itemsWithoutCost, Metric buyers, Metric newUsers) {}

    /** Work waiting on sellers right now (not limited to the period). */
    public record Operations(long awaitingPayment, BigDecimal awaitingPaymentAmount, long toPrepare, long toShip,
                             long inTransit, long balancesDue, BigDecimal balancesDueAmount) {}

    public record Catalog(long activeProducts, long outOfStockProducts, long variantsWithoutCost, long lowOwnStock) {}

    public record Reviews(BigDecimal average, long approved, long pending) {}

    public record DayPoint(LocalDate date, long orders, BigDecimal revenue) {}

    public record TopProduct(Long productId, String title, long units, BigDecimal revenue) {}

    public record RecentOrder(Long orderId, String buyerName, LocalDateTime createdAt, BigDecimal total,
                              String status, String paymentMethod, String channel, String stores) {}

    public record SupplierSync(String status, LocalDateTime startedAt, int disabled, int reenabled,
                               int costUpdates, int marginAlerts, String abortReason) {}
}
