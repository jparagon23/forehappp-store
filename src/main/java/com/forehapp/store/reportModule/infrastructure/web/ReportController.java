package com.forehapp.store.reportModule.infrastructure.web;

import com.forehapp.store.reportModule.application.dto.AdminDashboardResponse;
import com.forehapp.store.reportModule.application.dto.BusinessSummaryResponse;
import com.forehapp.store.reportModule.application.dto.RevenuePointResponse;
import com.forehapp.store.reportModule.application.dto.SellerSalesResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;
import com.forehapp.store.reportModule.application.usecases.AdminDashboardService;
import com.forehapp.store.reportModule.domain.ports.in.IAdminReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
public class ReportController {

    private final IAdminReportService adminReportService;
    private final AdminDashboardService dashboardService;

    public ReportController(IAdminReportService adminReportService, AdminDashboardService dashboardService) {
        this.adminReportService = adminReportService;
        this.dashboardService = dashboardService;
    }

    /** Admin home: last {days} days (default 30) vs the previous {days}, plus pending work and alerts. */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getDashboard(Long.parseLong(userId), days));
    }

    @GetMapping("/summary")
    public ResponseEntity<BusinessSummaryResponse> getSummary(
            @AuthenticationPrincipal String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(adminReportService.getBusinessSummary(Long.parseLong(userId), from, to));
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<RevenuePointResponse>> getRevenue(
            @AuthenticationPrincipal String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") String groupBy) {
        return ResponseEntity.ok(adminReportService.getRevenueByPeriod(Long.parseLong(userId), from, to, groupBy));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductResponse>> getTopProducts(
            @AuthenticationPrincipal String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminReportService.getTopProducts(Long.parseLong(userId), from, to, limit));
    }

    @GetMapping("/sellers")
    public ResponseEntity<List<SellerSalesResponse>> getSellerSales(
            @AuthenticationPrincipal String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(adminReportService.getSellerSalesReport(Long.parseLong(userId), from, to));
    }
}
