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
    private final int defaultRestock;

    public SupplierSyncPlanner(BigDecimal minMargin, double maxDisableRatio, int minDisableGuard, int defaultRestock) {
        this.minMargin = minMargin;
        this.maxDisableRatio = maxDisableRatio;
        this.minDisableGuard = minDisableGuard;
        this.defaultRestock = defaultRestock;
    }

    /**
     * @param links         CONFIRMED links (drive stock and cost) and SUGGESTED links (margin alerts only)
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
                planStock(link, item, actions, events);
                planCost(link, item, actions, events);
            }
            planMarginAlert(link, item, !isConfirmed, events);
        }

        String abortReason = null;
        long disables = actions.stream().filter(a -> a.type() == ActionType.DISABLE).count();
        long limit = Math.max(minDisableGuard, (long) Math.ceil(confirmed * maxDisableRatio));
        if (guardDisables && disables > limit) {
            abortReason = "Run would disable " + disables + " of " + confirmed
                    + " linked variants (limit " + limit + "). Check the supplier data before applying.";
        }
        return new SyncPlan(actions, events, abortReason);
    }

    private void planStock(LinkSnapshot link, ItemSnapshot item, List<Action> actions, List<EventDraft> events) {
        int stock = link.variantStock() == null ? 0 : link.variantStock();
        boolean disabledBySync = Boolean.TRUE.equals(link.disabledBySync());

        if (item.outOfStock()) {
            // A variant the seller hid is left alone; one restocked by hand while the supplier is
            // still out of stock is disabled again (dropshipping: nothing to ship)
            if (stock > 0 && Boolean.TRUE.equals(link.variantActive())) {
                actions.add(new Action(ActionType.DISABLE, link.linkId(), link.variantId(), link.productId(), stock, null));
                events.add(event(SyncEventType.DISABLED, link, item, String.valueOf(stock), "0",
                        "Out of stock at supplier", false));
            }
            return;
        }

        if (!disabledBySync) return;

        if (stock == 0) {
            int restore = link.stockBeforeSync() != null && link.stockBeforeSync() > 0
                    ? link.stockBeforeSync()
                    : defaultRestock;
            actions.add(new Action(ActionType.REENABLE, link.linkId(), link.variantId(), link.productId(), restore, null));
            events.add(event(SyncEventType.REENABLED, link, item, "0", String.valueOf(restore),
                    "Available again at supplier", false));
        } else {
            actions.add(new Action(ActionType.RELEASE, link.linkId(), link.variantId(), link.productId(), null, null));
            events.add(event(SyncEventType.RELEASED, link, item, null, String.valueOf(stock),
                    "Restocked by hand; sync stops tracking it", false));
        }
    }

    private void planCost(LinkSnapshot link, ItemSnapshot item, List<Action> actions, List<EventDraft> events) {
        if (item.price() == null) return;
        if (link.variantCost() != null && link.variantCost().compareTo(item.price()) == 0) return;

        actions.add(new Action(ActionType.UPDATE_COST, link.linkId(), link.variantId(), link.productId(), null, item.price()));
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
