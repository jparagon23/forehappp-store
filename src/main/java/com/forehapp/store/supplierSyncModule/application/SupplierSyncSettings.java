package com.forehapp.store.supplierSyncModule.application;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Getter
@Component
public class SupplierSyncSettings {

    private final String apiKey;
    private final BigDecimal minMargin;
    private final double maxDisableRatio;
    private final int minDisableGuard;
    private final double minCatalogRatio;
    private final double maxOutOfStockRatio;
    private final int defaultRestock;
    private final List<String> notifyEmails;

    public SupplierSyncSettings(
            @Value("${app.supplier-sync.api-key:}") String apiKey,
            @Value("${app.supplier-sync.min-margin:0.05}") BigDecimal minMargin,
            @Value("${app.supplier-sync.max-disable-ratio:0.25}") double maxDisableRatio,
            @Value("${app.supplier-sync.min-disable-guard:5}") int minDisableGuard,
            @Value("${app.supplier-sync.min-catalog-ratio:0.5}") double minCatalogRatio,
            @Value("${app.supplier-sync.max-out-of-stock-ratio:0.9}") double maxOutOfStockRatio,
            @Value("${app.supplier-sync.default-restock:10}") int defaultRestock,
            @Value("${app.supplier-sync.notify-emails:}") String notifyEmails) {
        this.apiKey = apiKey;
        this.minMargin = minMargin;
        this.maxDisableRatio = maxDisableRatio;
        this.minDisableGuard = minDisableGuard;
        this.minCatalogRatio = minCatalogRatio;
        this.maxOutOfStockRatio = maxOutOfStockRatio;
        this.defaultRestock = defaultRestock;
        this.notifyEmails = Arrays.stream(notifyEmails.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
