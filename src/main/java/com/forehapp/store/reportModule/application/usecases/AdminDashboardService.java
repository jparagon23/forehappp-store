package com.forehapp.store.reportModule.application.usecases;

import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.Kpis;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.Metric;
import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse.Period;
import com.forehapp.store.reportModule.infrastructure.persistence.AdminDashboardQueries;
import com.forehapp.store.reportModule.infrastructure.persistence.AdminDashboardQueries.ProfitTotals;
import com.forehapp.store.reportModule.infrastructure.persistence.AdminDashboardQueries.SalesTotals;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Admin home: the last N days compared with the N days before, plus what needs attention now. */
@Service
public class AdminDashboardService {

    private final AdminDashboardQueries queries;
    private final IStoreProfileDao storeProfileDao;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    public AdminDashboardService(AdminDashboardQueries queries, IStoreProfileDao storeProfileDao) {
        this.queries = queries;
        this.storeProfileDao = storeProfileDao;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard(Long userId, int days) {
        requireAdmin(userId);
        int span = Math.max(1, Math.min(days, 365));
        // Same clock the timestamps are stored with (server zone), so day boundaries match the data
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(span - 1L);
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        LocalDateTime prevFrom = from.minusDays(span);

        SalesTotals sales = queries.sales(from, to);
        SalesTotals prevSales = queries.sales(prevFrom, from);
        ProfitTotals profit = queries.profit(from, to);
        ProfitTotals prevProfit = queries.profit(prevFrom, from);

        Kpis kpis = new Kpis(
                new Metric(sales.revenue(), prevSales.revenue()),
                new Metric(BigDecimal.valueOf(sales.orders()), BigDecimal.valueOf(prevSales.orders())),
                new Metric(average(sales), average(prevSales)),
                new Metric(profit.profit(), prevProfit.profit()),
                profit.items(), profit.itemsWithoutCost(),
                new Metric(BigDecimal.valueOf(sales.buyers()), BigDecimal.valueOf(prevSales.buyers())),
                new Metric(BigDecimal.valueOf(queries.newUsers(from, to)), BigDecimal.valueOf(queries.newUsers(prevFrom, from))));

        return new AdminDashboardResponse(
                new Period(fromDate, today, span),
                kpis,
                queries.operations(),
                queries.catalog(lowStockThreshold),
                queries.reviews(),
                queries.series(from, to),
                queries.topProducts(from, to, 5),
                queries.recentOrders(8),
                queries.lastSupplierSync());
    }

    private static BigDecimal average(SalesTotals s) {
        return s.orders() == 0 ? BigDecimal.ZERO
                : s.revenue().divide(BigDecimal.valueOf(s.orders()), 2, RoundingMode.HALF_UP);
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Admin access required");
        }
    }
}
