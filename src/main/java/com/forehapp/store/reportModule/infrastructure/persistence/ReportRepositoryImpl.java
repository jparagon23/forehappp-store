package com.forehapp.store.reportModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.OrderStatus;
import com.forehapp.store.reportModule.application.dto.RevenuePointResponse;
import com.forehapp.store.reportModule.application.dto.SellerSalesResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;
import com.forehapp.store.reportModule.domain.ports.out.IReportDao;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReportRepositoryImpl implements IReportDao {

    private final IReportRepository repository;

    public ReportRepositoryImpl(IReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public Long countOrdersByStatus(OrderStatus status, LocalDateTime from, LocalDateTime to) {
        return repository.countOrdersByStatus(status, from, to);
    }

    @Override
    public BigDecimal sumRevenueByStatus(OrderStatus status, LocalDateTime from, LocalDateTime to) {
        return repository.sumRevenueByStatus(status, from, to);
    }

    @Override
    public BigDecimal avgTicketByStatus(OrderStatus status, LocalDateTime from, LocalDateTime to) {
        return repository.avgTicketByStatus(status, from, to);
    }

    @Override
    public Long countApprovedReturns(LocalDateTime from, LocalDateTime to) {
        return repository.countApprovedReturns(from, to);
    }

    @Override
    public BigDecimal sumRefunded(LocalDateTime from, LocalDateTime to) {
        return repository.sumRefunded(from, to);
    }

    @Override
    public List<RevenuePointResponse> getRevenueByDay(LocalDateTime from, LocalDateTime to) {
        return repository.getRevenueByDay(from, to).stream()
                .map(p -> new RevenuePointResponse(p.getPeriod(), p.getOrderCount(), p.getRevenue()))
                .toList();
    }

    @Override
    public List<RevenuePointResponse> getRevenueByWeek(LocalDateTime from, LocalDateTime to) {
        return repository.getRevenueByWeek(from, to).stream()
                .map(p -> new RevenuePointResponse(p.getPeriod(), p.getOrderCount(), p.getRevenue()))
                .toList();
    }

    @Override
    public List<RevenuePointResponse> getRevenueByMonth(LocalDateTime from, LocalDateTime to) {
        return repository.getRevenueByMonth(from, to).stream()
                .map(p -> new RevenuePointResponse(p.getPeriod(), p.getOrderCount(), p.getRevenue()))
                .toList();
    }

    @Override
    public List<TopProductResponse> getTopProducts(LocalDateTime from, LocalDateTime to, int limit) {
        return repository.getTopProducts(from, to, limit).stream()
                .map(p -> new TopProductResponse(
                        p.getProductId(), p.getProductTitle(), p.getVariantSku(),
                        p.getUnitsSold(), p.getRevenue()))
                .toList();
    }

    @Override
    public List<SellerSalesResponse> getSellerSales(LocalDateTime from, LocalDateTime to) {
        return repository.getSellerSales(from, to).stream()
                .map(p -> new SellerSalesResponse(
                        p.getStoreId(), p.getStoreName(),
                        p.getTotalOrders(), p.getTotalRevenue()))
                .toList();
    }

    @Override
    public Long countSellerOrders(Long storeId, LocalDateTime from, LocalDateTime to) {
        return repository.countSellerOrders(storeId, from, to);
    }

    @Override
    public BigDecimal sumSellerRevenue(Long storeId, LocalDateTime from, LocalDateTime to) {
        return repository.sumSellerRevenue(storeId, from, to);
    }

    @Override
    public BigDecimal avgSellerTicket(Long storeId, LocalDateTime from, LocalDateTime to) {
        return repository.avgSellerTicket(storeId, from, to);
    }

    @Override
    public Long countSellerReturns(Long storeId, LocalDateTime from, LocalDateTime to) {
        return repository.countSellerReturns(storeId, from, to);
    }

    @Override
    public BigDecimal sumSellerRefunded(Long storeId, LocalDateTime from, LocalDateTime to) {
        return repository.sumSellerRefunded(storeId, from, to);
    }

    @Override
    public List<TopProductResponse> getSellerTopProducts(Long storeId, LocalDateTime from, LocalDateTime to, int limit) {
        return repository.getSellerTopProducts(storeId, from, to, limit).stream()
                .map(p -> new TopProductResponse(
                        p.getProductId(), p.getProductTitle(), p.getVariantSku(),
                        p.getUnitsSold(), p.getRevenue()))
                .toList();
    }
}
