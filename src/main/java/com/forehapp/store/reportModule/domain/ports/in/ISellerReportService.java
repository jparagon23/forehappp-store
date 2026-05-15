package com.forehapp.store.reportModule.domain.ports.in;

import com.forehapp.store.reportModule.application.dto.BusinessSummaryResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;

import java.time.LocalDate;
import java.util.List;

public interface ISellerReportService {
    BusinessSummaryResponse getMySummary(Long userId, LocalDate from, LocalDate to);
    List<TopProductResponse> getMyTopProducts(Long userId, LocalDate from, LocalDate to, int limit);
}
