package com.forehapp.store.reportModule.infrastructure.web;

import com.forehapp.store.reportModule.application.dto.BusinessSummaryResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;
import com.forehapp.store.reportModule.domain.ports.in.ISellerReportService;
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
@RequestMapping("/api/v1/seller/reports")
public class ReportSellerController {

    private final ISellerReportService sellerReportService;

    public ReportSellerController(ISellerReportService sellerReportService) {
        this.sellerReportService = sellerReportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<BusinessSummaryResponse> getMySummary(
            @AuthenticationPrincipal String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(sellerReportService.getMySummary(Long.parseLong(userId), from, to));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductResponse>> getMyTopProducts(
            @AuthenticationPrincipal String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(sellerReportService.getMyTopProducts(Long.parseLong(userId), from, to, limit));
    }
}
