package com.forehapp.store.repurchaseModule.application.usecases;

import com.forehapp.store.repurchaseModule.domain.model.PurchaseRow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class RepurchaseCalculator {

    private RepurchaseCalculator() {}

    /** Days one unit lasts: the variant override wins over the product value; null when neither is set. */
    public static Integer unitDays(PurchaseRow row) {
        return row.variantRepurchaseDays() != null ? row.variantRepurchaseDays() : row.productRepurchaseDays();
    }

    /** Total days the purchased supply lasts (unit days × quantity, summed), capped; 0 when no row has a duration. */
    public static int durationDays(List<PurchaseRow> rows, int maxDurationDays) {
        long total = 0;
        for (PurchaseRow row : rows) {
            Integer unitDays = unitDays(row);
            if (unitDays != null) {
                total += (long) unitDays * row.quantity();
            }
        }
        return (int) Math.min(total, maxDurationDays);
    }

    /** Day the buyer started using the product: delivery date, or shipping date plus a fallback transit time. */
    public static Optional<LocalDate> anchorDate(List<PurchaseRow> rows, int shippedFallbackDays) {
        Optional<LocalDate> delivered = rows.stream()
                .map(PurchaseRow::deliveredAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toLocalDate);
        if (delivered.isPresent()) {
            return delivered;
        }
        return rows.stream()
                .map(PurchaseRow::shippedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .map(shipped -> shipped.toLocalDate().plusDays(shippedFallbackDays));
    }

    /** Day the reminder should go out: once sendRatio of the duration has elapsed since the anchor. */
    public static LocalDate dueDate(LocalDate anchor, int durationDays, double sendRatio) {
        return anchor.plusDays(Math.max(1, Math.round(durationDays * sendRatio)));
    }
}
