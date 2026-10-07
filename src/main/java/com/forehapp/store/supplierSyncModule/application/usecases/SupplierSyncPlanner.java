package com.forehapp.store.supplierSyncModule.application.usecases;

import com.forehapp.store.supplierSyncModule.domain.model.ItemSnapshot;
import com.forehapp.store.supplierSyncModule.domain.model.LinkSnapshot;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLinkStatus;
import com.forehapp.store.supplierSyncModule.domain.model.SyncEventType;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan.Action;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan.ActionType;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan.EventDraft;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Decides what a sync run should change for one store. Pure logic: no database access. */
public class SupplierSyncPlanner {

    private final BigDecimal minMargin;
    private final double maxDisableRatio;
    private final int minDisableGuard;

    public SupplierSyncPlanner(BigDecimal minMargin, double maxDisableRatio, int minDisableGuard) {
        this.minMargin = minMargin;
        this.maxDisableRatio = maxDisableRatio;
        this.minDisableGuard = minDisableGuard;
    }

    /**
     * @param links         CONFIRMED links (drive supplier availability and cost) and SUGGESTED links (margin alerts only)
     * @param items         supplier products by id
     * @param guardDisables when true, too many disables in one run aborts it (off for a store's first applied run,
     *                      where disabling a large share of the catalog is expected)
     */
    public SyncPlan plan(List<LinkSnapshot> links, Map<Long, ItemSnapshot> items, boolean guardDisables) {
        List<Action> actions = new ArrayList<>();
        List<EventDraft> events = new ArrayList<>();
        int confirmed = 0;

        for (LinkSnapshot link : links) {
            ItemSnapshot item = link.supplierItemId() == null ? null : items.get(link.supplierItemId());
            boolean isConfirmed = link.status() == SupplierLinkStatus.CONFIRMED;
            if (isConfirmed) confirmed++;

            if (item == null || !item.seenInRun()) {
                if (isConfirmed) {
                    events.add(event(SyncEventType.BROKEN_LINK, link, item, null, null,
                            "Supplier product not found in the latest catalog", false));
                }
                continue;
            }

            if (isConfirmed) {
                planAvailability(link, item, actions, events);
                planCost(link, item, actions, events);
            }
            planMarginAlert(link, item, !isConfirmed, events);
        }

        String abortReason = null;
        long disables = events.stream().filter(e -> e.type() == SyncEventType.DISABLED).count();
        long limit = Math.max(minDisableGuard, (long) Math.ceil(confirmed * maxDisableRatio));
        if (guardDisables && disables > limit) {
            abortReason = "Run would disable " + disables + " of " + confirmed
                    + " linked variants (limit " + limit + "). Check the supplier data before applying.";
        }
        return new SyncPlan(actions, events, abortReason);
    }

    /**
     * Mirrors the supplier's availability on the variant. The own stock is never touched: it keeps selling
     * while the supplier is out. Only active dropship variants change what can be sold, so only they are reported.
     */
    private void planAvailability(LinkSnapshot link, ItemSnapshot item, List<Action> actions, List<EventDraft> events) {
        boolean available = !item.outOfStock();
        if (available == !Boolean.FALSE.equals(link.variantSupplierAvailable())) return;

        actions.add(new Action(available ? ActionType.MARK_AVAILABLE : ActionType.MARK_UNAVAILABLE,
                link.linkId(), link.variantId(), link.productId(), null));

        if (Boolean.TRUE.equals(link.variantDropship()) && Boolean.TRUE.equals(link.variantActive())) {
            String ownStock = String.valueOf(link.variantStock() == null ? 0 : link.variantStock());
            events.add(available
                    ? event(SyncEventType.REENABLED, link, item, null, ownStock, "Available again at supplier", false)
                    : event(SyncEventType.DISABLED, link, item, null, ownStock, "Out of stock at supplier", false));
        }
    }

    private void planCost(LinkSnapshot link, ItemSnapshot item, List<Action> actions, List<EventDraft> events) {
        if (item.price() == null) return;
        if (link.variantCost() != null && link.variantCost().compareTo(item.price()) == 0) return;

        actions.add(new Action(ActionType.UPDATE_COST, link.linkId(), link.variantId(), link.productId(), item.price()));
        events.add(event(SyncEventType.COST_UPDATED, link, item,
                link.variantCost() == null ? null : link.variantCost().toPlainString(),
                item.price().toPlainString(), null, false));
    }

    private void planMarginAlert(LinkSnapshot link, ItemSnapshot item, boolean unconfirmed, List<EventDraft> events) {
        BigDecimal price = link.variantPrice();
        if (item.price() == null || price == null || price.signum() <= 0) return;
        if (!Boolean.TRUE.equals(link.variantActive())) return;

        BigDecimal margin = price.subtract(item.price()).divide(price, 4, RoundingMode.HALF_UP);
        if (margin.compareTo(minMargin) < 0) {
            events.add(event(SyncEventType.MARGIN_ALERT, link, item,
                    price.toPlainString() + " / " + item.price().toPlainString(),
                    margin.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%",
                    "Sale price / supplier price", unconfirmed));
        }
    }

    private static EventDraft event(SyncEventType type, LinkSnapshot link, ItemSnapshot item,
                                    String oldValue, String newValue, String detail, boolean unconfirmed) {
        return new EventDraft(type, link.variantId(), link.productId(), link.productTitle(), link.variantLabel(),
                item == null ? null : item.name(), oldValue, newValue, detail, unconfirmed);
    }
}
