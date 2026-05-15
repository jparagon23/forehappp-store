package com.forehapp.store.reportModule.domain.ports.in;

import com.forehapp.store.reportModule.application.dto.BusinessSummaryResponse;
import com.forehapp.store.reportModule.application.dto.RevenuePointResponse;
import com.forehapp.store.reportModule.application.dto.SellerSalesResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;

import java.time.LocalDate;
import java.util.List;

public interface IAdminReportService {
    BusinessSummaryResponse getBusinessSummary(Long userId, LocalDate from, LocalDate to);
    List<RevenuePointResponse> getRevenueByPeriod(Long userId, LocalDate from, LocalDate to, String groupBy);
    List<TopProductResponse> getTopProducts(Long userId, LocalDate from, LocalDate to, int limit);
    List<SellerSalesResponse> getSellerSalesReport(Long userId, LocalDate from, LocalDate to);
}
