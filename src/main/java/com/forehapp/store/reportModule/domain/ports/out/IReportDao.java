package com.forehapp.store.reportModule.domain.ports.out;

import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.domain.model.OrderStatus;
import com.forehapp.store.reportModule.application.dto.LowStockItemResponse;
import com.forehapp.store.reportModule.application.dto.RevenuePointResponse;
import com.forehapp.store.reportModule.application.dto.SellerSalesResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IReportDao {

    // --- Admin: summary scalars ---
    Long countOrdersByStatus(OrderStatus status, LocalDateTime from, LocalDateTime to);
    BigDecimal sumRevenueByStatus(OrderStatus status, LocalDateTime from, LocalDateTime to);
    BigDecimal avgTicketByStatus(OrderStatus status, LocalDateTime from, LocalDateTime to);
    Long countApprovedReturns(LocalDateTime from, LocalDateTime to);
    BigDecimal sumRefunded(LocalDateTime from, LocalDateTime to);

    // --- Admin: revenue series ---
    List<RevenuePointResponse> getRevenueByDay(LocalDateTime from, LocalDateTime to);
    List<RevenuePointResponse> getRevenueByWeek(LocalDateTime from, LocalDateTime to);
    List<RevenuePointResponse> getRevenueByMonth(LocalDateTime from, LocalDateTime to);

    // --- Admin: rankings ---
    List<TopProductResponse> getTopProducts(LocalDateTime from, LocalDateTime to, int limit);
    List<SellerSalesResponse> getSellerSales(LocalDateTime from, LocalDateTime to);

    // --- Seller: summary scalars ---
    Long countSellerOrders(Long storeId, LocalDateTime from, LocalDateTime to);
    BigDecimal sumSellerRevenue(Long storeId, LocalDateTime from, LocalDateTime to);
    BigDecimal avgSellerTicket(Long storeId, LocalDateTime from, LocalDateTime to);
    Long countSellerReturns(Long storeId, LocalDateTime from, LocalDateTime to);
    BigDecimal sumSellerRefunded(Long storeId, LocalDateTime from, LocalDateTime to);

    // --- Seller: group status counts ---
    Long countSellerGroupsByStatus(Long storeId, OrderSellerGroupStatus status, LocalDateTime from, LocalDateTime to);

    // --- Seller: low stock ---
    List<LowStockItemResponse> getLowStockByStore(Long storeId, int threshold);

    // --- Seller: rankings ---
    List<TopProductResponse> getSellerTopProducts(Long storeId, LocalDateTime from, LocalDateTime to, int limit);
}
