package com.forehapp.store.supplierSyncModule;

import com.forehapp.store.supplierSyncModule.application.usecases.SupplierNames;
import com.forehapp.store.supplierSyncModule.application.usecases.SupplierSyncPlanner;
import com.forehapp.store.supplierSyncModule.domain.model.ItemSnapshot;
import com.forehapp.store.supplierSyncModule.domain.model.LinkSnapshot;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLinkStatus;
import com.forehapp.store.supplierSyncModule.domain.model.SyncEventType;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan.ActionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplierSyncPlannerTest {

    // 5% minimum margin, guard at max(5, 25% of confirmed)
    private final SupplierSyncPlanner planner = new SupplierSyncPlanner(new BigDecimal("0.05"), 0.25, 5);

    @Test
    void marksDropshipVariantUnavailableWhenSupplierIsOutOfStock() {
        SyncPlan plan = plan(link(1L).stock(3).build(), item(100L).outOfStock().build());

        assertEquals(List.of(ActionType.MARK_UNAVAILABLE), types(plan));
        assertEquals(1, plan.count(SyncEventType.DISABLED));
        // Own stock keeps selling and is reported, never changed
        assertEquals("3", plan.events().get(0).newValue());
    }

    @Test
    void marksAvailableAgainWhenSupplierRestocks() {
        SyncPlan plan = plan(link(1L).unavailable().build(), item(100L).build());

        assertEquals(List.of(ActionType.MARK_AVAILABLE), types(plan));
        assertEquals(1, plan.count(SyncEventType.REENABLED));
    }

    @Test
    void doesNothingWhenAvailabilityAlreadyMatches() {
        assertTrue(types(plan(link(1L).build(), item(100L).build())).isEmpty());
        assertTrue(types(plan(link(1L).unavailable().build(), item(100L).outOfStock().build())).isEmpty());
    }

    @Test
    void keepsAvailabilityOfOwnStockOnlyVariantsWithoutReportingIt() {
        SyncPlan plan = plan(link(1L).ownStockOnly().build(), item(100L).outOfStock().build());

        assertEquals(List.of(ActionType.MARK_UNAVAILABLE), types(plan));
        assertEquals(0, plan.count(SyncEventType.DISABLED));
    }

    @Test
    void doesNotReportVariantHiddenBySeller() {
        SyncPlan plan = plan(link(1L).inactive().build(), item(100L).outOfStock().build());

        assertEquals(List.of(ActionType.MARK_UNAVAILABLE), types(plan));
        assertEquals(0, plan.count(SyncEventType.DISABLED));
    }

    @Test
    void updatesCostOnlyWhenSupplierPriceChanged() {
        SyncPlan changed = plan(link(1L).cost("40000").build(), item(100L).price("42000").build());
        SyncPlan same = plan(link(1L).cost("42000").build(), item(100L).price("42000.00").build());

        assertEquals(List.of(ActionType.UPDATE_COST), types(changed));
        assertEquals(new BigDecimal("42000"), changed.actions().get(0).cost());
        assertTrue(types(same).isEmpty());
    }

    @Test
    void reportsLowMarginFromSupplierPrice() {
        // (65000 - 62200) / 65000 = 4.31%
        SyncPlan plan = plan(link(1L).price("65000").cost("62200").build(), item(100L).price("62200").build());

        assertEquals(1, plan.count(SyncEventType.MARGIN_ALERT));
        assertEquals("4.31%", plan.events().get(0).newValue());
    }

    @Test
    void suggestedPairsOnlyProduceMarginAlertsMarkedUnconfirmed() {
        SyncPlan plan = plan(link(1L).suggested().price("65000").build(),
                item(100L).outOfStock().price("64000").build());

        assertTrue(types(plan).isEmpty());
        assertEquals(1, plan.count(SyncEventType.MARGIN_ALERT));
        assertTrue(plan.events().get(0).unconfirmed());
    }

    @Test
    void reportsBrokenLinkAndDoesNothingWhenItemWasNotInTheCatalog() {
        SyncPlan plan = plan(link(1L).build(), item(100L).outOfStock().notSeen().build());

        assertTrue(types(plan).isEmpty());
        assertEquals(1, plan.count(SyncEventType.BROKEN_LINK));
    }

    @Test
    void guardAbortsWhenTooManyVariantsWouldBeDisabledAtOnce() {
        List<LinkSnapshot> links = new ArrayList<>();
        Map<Long, ItemSnapshot> items = new HashMap<>();
        for (long i = 1; i <= 20; i++) {
            links.add(link(i).item(100 + i).build());
            items.put(100 + i, item(100 + i).outOfStock(i <= 6).build());
        }

        // 6 disables of 20 confirmed: above max(5, 25% of 20 = 5)
        SyncPlan guarded = planner.plan(links, items, true);
        SyncPlan unguarded = planner.plan(links, items, false);

        assertTrue(guarded.aborted());
        assertFalse(unguarded.aborted());
        assertEquals(6, unguarded.count(SyncEventType.DISABLED));
    }

    @Test
    void guardIgnoresOwnStockOnlyVariants() {
        List<LinkSnapshot> links = new ArrayList<>();
        Map<Long, ItemSnapshot> items = new HashMap<>();
        for (long i = 1; i <= 20; i++) {
            links.add(link(i).item(100 + i).ownStockOnly().build());
            items.put(100 + i, item(100 + i).outOfStock().build());
        }

        assertFalse(planner.plan(links, items, true).aborted());
    }

    @Test
    void guardAllowsSmallNumberOfDisables() {
        List<LinkSnapshot> links = new ArrayList<>();
        Map<Long, ItemSnapshot> items = new HashMap<>();
        for (long i = 1; i <= 4; i++) {
            links.add(link(i).item(100 + i).build());
            items.put(100 + i, item(100 + i).outOfStock().build());
        }

        assertNull(planner.plan(links, items, true).abortReason());
    }

    @Test
    void normalizesSupplierNamesForMatching() {
        assertEquals("whey gold standard 2lb cookies and cream",
                SupplierNames.normalize("  Whey Gold Standard 2Lb  cookies-And Cream "));
        assertEquals("aminoacidos bcaa", SupplierNames.normalize("Aminoácidos — BCAA"));
    }

    // ── Builders ──────────────────────────────────────────────────

    private SyncPlan plan(LinkSnapshot link, ItemSnapshot item) {
        return planner.plan(List.of(link), Map.of(item.id(), item), false);
    }

    private static List<ActionType> types(SyncPlan plan) {
        return plan.actions().stream().map(SyncPlan.Action::type).toList();
    }

    private static LinkBuilder link(Long id) {
        return new LinkBuilder(id);
    }

    private static ItemBuilder item(Long id) {
        return new ItemBuilder(id);
    }

    private static final class LinkBuilder {
        private final Long id;
        private Long itemId = 100L;
        private SupplierLinkStatus status = SupplierLinkStatus.CONFIRMED;
        private boolean active = true;
        private int stock = 0;
        private boolean dropship = true;
        private boolean supplierAvailable = true;
        private BigDecimal price = new BigDecimal("100000");
        private BigDecimal cost = new BigDecimal("50000");

        private LinkBuilder(Long id) { this.id = id; }

        LinkBuilder item(Long value) { itemId = value; return this; }
        LinkBuilder suggested() { status = SupplierLinkStatus.SUGGESTED; return this; }
        LinkBuilder inactive() { active = false; return this; }
        LinkBuilder stock(int value) { stock = value; return this; }
        LinkBuilder price(String value) { price = new BigDecimal(value); return this; }
        LinkBuilder cost(String value) { cost = new BigDecimal(value); return this; }
        LinkBuilder ownStockOnly() { dropship = false; return this; }
        LinkBuilder unavailable() { supplierAvailable = false; return this; }

        LinkSnapshot build() {
            return new LinkSnapshot(id, 1000 + id, 2000 + id, "Product " + id, "SKU-" + id, active, stock,
                    dropship, supplierAvailable, price, cost, status, itemId);
        }
    }

    private static final class ItemBuilder {
        private final Long id;
        private BigDecimal price = new BigDecimal("50000");
        private boolean outOfStock;
        private boolean seen = true;

        private ItemBuilder(Long id) { this.id = id; }

        ItemBuilder price(String value) { price = new BigDecimal(value); return this; }
        ItemBuilder outOfStock() { outOfStock = true; return this; }
        ItemBuilder outOfStock(boolean value) { outOfStock = value; return this; }
        ItemBuilder notSeen() { seen = false; return this; }

        ItemSnapshot build() {
            return new ItemSnapshot(id, "Supplier " + id, price, outOfStock, seen);
        }
    }
}
