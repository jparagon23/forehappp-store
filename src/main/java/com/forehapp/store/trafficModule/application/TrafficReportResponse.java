package com.forehapp.store.trafficModule.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Traffic for the last N days next to the N days before. Rates are percentages (0-100).
 * bounce: sessions with a single page and under 10 seconds.
 */
public record TrafficReportResponse(
        LocalDate from,
        LocalDate to,
        int days,
        long liveNow,
        Totals current,
        Totals previous,
        List<DayPoint> series,
        Funnel funnel,
        List<Page> topPages,
        List<Share> sources,
        List<Share> devices
) {
    public record Totals(long visitors, long sessions, long pageViews, BigDecimal avgDurationSeconds,
                         BigDecimal pagesPerSession, BigDecimal bounceRate, BigDecimal conversionRate,
                         long returningVisitors) {}

    public record DayPoint(LocalDate date, long visitors, long sessions) {}

    public record Funnel(long sessions, long viewedProduct, long addedToCart, long reachedCheckout, long purchased) {}

    /** title: product name for /product/{id} pages, null otherwise. */
    public record Page(String path, String title, long views, long sessions) {}

    public record Share(String name, long sessions, long purchases) {}
}
