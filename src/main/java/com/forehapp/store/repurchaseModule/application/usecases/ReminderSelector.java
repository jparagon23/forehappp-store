package com.forehapp.store.repurchaseModule.application.usecases;

import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.repurchaseModule.application.RepurchaseSettings;
import com.forehapp.store.repurchaseModule.domain.model.PurchaseRow;
import com.forehapp.store.repurchaseModule.domain.model.ReminderPlan;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Decides who gets a repurchase reminder today. Pure logic: no database or email access. */
@Component
public class ReminderSelector {

    private final RepurchaseSettings settings;

    public ReminderSelector(RepurchaseSettings settings) {
        this.settings = settings;
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Groups purchases by buyer and keeps the products whose reminder is due today (or within the late window). */
    public List<ReminderPlan> findDuePlans(List<PurchaseRow> rows, Set<Long> remindedItemIds, LocalDate today) {
        Map<String, List<PurchaseRow>> byEmail = rows.stream()
                .filter(r -> !normalizeEmail(r.email()).isEmpty())
                .collect(Collectors.groupingBy(r -> normalizeEmail(r.email()), TreeMap::new, Collectors.toList()));

        List<ReminderPlan> plans = new ArrayList<>();
        for (Map.Entry<String, List<PurchaseRow>> entry : byEmail.entrySet()) {
            Map<Long, List<PurchaseRow>> byProduct = entry.getValue().stream()
                    .collect(Collectors.groupingBy(PurchaseRow::productId, TreeMap::new, Collectors.toList()));

            List<ReminderPlan.Item> items = new ArrayList<>();
            for (List<PurchaseRow> productRows : byProduct.values()) {
                dueItem(productRows, remindedItemIds, today).ifPresent(items::add);
            }
            if (items.isEmpty()) continue;

            items.sort(Comparator.comparing(ReminderPlan.Item::dueDate));
            plans.add(new ReminderPlan(entry.getKey(), resolveName(entry.getValue()), items));
        }
        return plans;
    }

    /** Applies the per-buyer limits: opt-out, minimum gap between emails, and pause after ignored reminders. */
    public List<ReminderPlan> applyRecipientRules(List<ReminderPlan> plans,
                                                  Set<String> unsubscribed,
                                                  Map<String, Set<LocalDateTime>> sentAtByEmail,
                                                  Map<String, LocalDateTime> lastOrderAtByEmail,
                                                  LocalDate today) {
        LocalDate earliestAllowedLastSend = today.minusDays(settings.getMinDaysBetweenEmails());
        List<ReminderPlan> allowed = new ArrayList<>();
        for (ReminderPlan plan : plans) {
            if (unsubscribed.contains(plan.email())) continue;

            Set<LocalDateTime> sentAt = sentAtByEmail.getOrDefault(plan.email(), Set.of());
            boolean sentRecently = sentAt.stream()
                    .anyMatch(sent -> sent.toLocalDate().isAfter(earliestAllowedLastSend));
            if (sentRecently) continue;

            LocalDateTime lastOrderAt = lastOrderAtByEmail.get(plan.email());
            long ignored = sentAt.stream()
                    .filter(sent -> lastOrderAt == null || sent.isAfter(lastOrderAt))
                    .count();
            if (ignored >= settings.getMaxIgnoredReminders()) continue;

            allowed.add(plan);
        }
        return allowed;
    }

    private Optional<ReminderPlan.Item> dueItem(List<PurchaseRow> productRows, Set<Long> remindedItemIds, LocalDate today) {
        // Only the most recent purchase of the product matters: older ones were already repurchased
        PurchaseRow latest = productRows.stream()
                .max(Comparator.comparing(PurchaseRow::orderCreatedAt).thenComparing(PurchaseRow::orderId))
                .orElseThrow();
        List<PurchaseRow> orderRows = productRows.stream()
                .filter(r -> r.orderId().equals(latest.orderId()))
                .sorted(Comparator.comparing(PurchaseRow::itemId))
                .toList();

        if (orderRows.stream().anyMatch(r -> remindedItemIds.contains(r.itemId()))) return Optional.empty();
        if (latest.productStatus() != ProductStatus.ACTIVE) return Optional.empty();

        int durationDays = RepurchaseCalculator.durationDays(orderRows, settings.getMaxDurationDays());
        if (durationDays <= 0) return Optional.empty();

        Optional<LocalDate> anchor = RepurchaseCalculator.anchorDate(orderRows, settings.getShippedFallbackDays());
        if (anchor.isEmpty()) return Optional.empty();

        LocalDate dueDate = RepurchaseCalculator.dueDate(anchor.get(), durationDays, settings.getSendRatio());
        if (dueDate.isAfter(today) || dueDate.isBefore(today.minusDays(settings.getLateWindowDays()))) {
            return Optional.empty();
        }

        Optional<PurchaseRow> buyable = orderRows.stream()
                .filter(r -> Boolean.TRUE.equals(r.variantActive()) && Boolean.TRUE.equals(r.variantInStock()))
                .findFirst();
        if (buyable.isEmpty()) return Optional.empty();

        return Optional.of(new ReminderPlan.Item(
                latest.productId(),
                latest.productTitle(),
                buyable.get().variantPrice(),
                orderRows.stream().mapToInt(PurchaseRow::quantity).sum(),
                latest.orderId(),
                anchor.get(),
                dueDate,
                orderRows.stream().map(PurchaseRow::itemId).toList()));
    }

    private String resolveName(List<PurchaseRow> rows) {
        PurchaseRow latest = rows.stream()
                .max(Comparator.comparing(PurchaseRow::orderCreatedAt))
                .orElseThrow();
        String name = latest.buyerName() != null ? latest.buyerName() : latest.guestName();
        return name == null ? "" : name.trim();
    }
}
