package com.forehapp.store.reportModule.application.usecases;

import com.forehapp.store.orderModule.domain.model.OrderStatus;
import com.forehapp.store.reportModule.application.dto.BusinessSummaryResponse;
import com.forehapp.store.reportModule.application.dto.RevenuePointResponse;
import com.forehapp.store.reportModule.application.dto.SellerSalesResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;
import com.forehapp.store.reportModule.domain.ports.in.IAdminReportService;
import com.forehapp.store.reportModule.domain.ports.out.IReportDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AdminReportServiceImpl implements IAdminReportService {

    private final IReportDao reportDao;
    private final IStoreProfileDao storeProfileDao;

    public AdminReportServiceImpl(IReportDao reportDao, IStoreProfileDao storeProfileDao) {
        this.reportDao = reportDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessSummaryResponse getBusinessSummary(Long userId, LocalDate from, LocalDate to) {
        requireAdmin(userId);
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        return new BusinessSummaryResponse(
                reportDao.countOrdersByStatus(OrderStatus.PAID, fromDt, toDt),
                reportDao.sumRevenueByStatus(OrderStatus.PAID, fromDt, toDt),
                reportDao.avgTicketByStatus(OrderStatus.PAID, fromDt, toDt),
                reportDao.countOrdersByStatus(OrderStatus.CANCELLED, fromDt, toDt),
                reportDao.countApprovedReturns(fromDt, toDt),
                reportDao.sumRefunded(fromDt, toDt)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenuePointResponse> getRevenueByPeriod(Long userId, LocalDate from, LocalDate to, String groupBy) {
        requireAdmin(userId);
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        return switch (groupBy.toUpperCase()) {
            case "WEEK"  -> reportDao.getRevenueByWeek(fromDt, toDt);
            case "MONTH" -> reportDao.getRevenueByMonth(fromDt, toDt);
            default      -> reportDao.getRevenueByDay(fromDt, toDt);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductResponse> getTopProducts(Long userId, LocalDate from, LocalDate to, int limit) {
        requireAdmin(userId);
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return reportDao.getTopProducts(from.atStartOfDay(), to.atTime(LocalTime.MAX), safeLimit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerSalesResponse> getSellerSalesReport(Long userId, LocalDate from, LocalDate to) {
        requireAdmin(userId);
        return reportDao.getSellerSales(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
