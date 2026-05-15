package com.forehapp.store.reportModule.domain.ports.out;

import com.forehapp.store.reportModule.application.dto.RevenuePointResponse;
import com.forehapp.store.reportModule.application.dto.SellerSalesResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IReportDao {

    // --- Admin: summary scalars ---
    Long countOrdersByStatus(String status, LocalDateTime from, LocalDateTime to);
    BigDecimal sumRevenueByStatus(String status, LocalDateTime from, LocalDateTime to);
    BigDecimal avgTicketByStatus(String status, LocalDateTime from, LocalDateTime to);
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
    Long countSellerOrders(Long sellerId, LocalDateTime from, LocalDateTime to);
    BigDecimal sumSellerRevenue(Long sellerId, LocalDateTime from, LocalDateTime to);
    BigDecimal avgSellerTicket(Long sellerId, LocalDateTime from, LocalDateTime to);
    Long countSellerReturns(Long sellerId, LocalDateTime from, LocalDateTime to);
    BigDecimal sumSellerRefunded(Long sellerId, LocalDateTime from, LocalDateTime to);

    // --- Seller: rankings ---
    List<TopProductResponse> getSellerTopProducts(Long sellerId, LocalDateTime from, LocalDateTime to, int limit);
}
