package com.forehapp.store.repurchaseModule;

import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.repurchaseModule.application.RepurchaseSettings;
import com.forehapp.store.repurchaseModule.application.usecases.ReminderSelector;
import com.forehapp.store.repurchaseModule.domain.model.PurchaseRow;
import com.forehapp.store.repurchaseModule.domain.model.ReminderPlan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReminderSelectorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);
    private static final String EMAIL = "buyer@mail.com";

    // ratio 0.8, late window 7d, 30d between emails, pause after 2 ignored, cap 365d, shipped fallback 3d
    private final ReminderSelector selector =
            new ReminderSelector(new RepurchaseSettings(true, 0.8, 7, 30, 2, 365, 3));

    @Test
    void remindsWhenEightyPercentOfDurationElapsed() {
        // 2 units x 30 days = 60 days, delivered Oct 1 -> due on day 48 = Nov 18
        PurchaseRow row = row(1L, 10L, 2).deliveredOn(LocalDate.of(2026, 10, 1)).build();

        List<ReminderPlan> plans = selector.findDuePlans(List.of(row), Set.of(), TODAY);

        assertEquals(1, plans.size());
        ReminderPlan.Item item = plans.get(0).items().get(0);
        assertEquals(EMAIL, plans.get(0).email());
        assertEquals(LocalDate.of(2026, 11, 18), item.dueDate());
        assertEquals(2, item.quantity());
        assertEquals(List.of(1L), item.orderItemIds());
    }

    @Test
    void doesNotRemindBeforeDueDate() {
        PurchaseRow row = row(1L, 10L, 2).deliveredOn(LocalDate.of(2026, 10, 2)).build();

        assertTrue(selector.findDuePlans(List.of(row), Set.of(), TODAY).isEmpty());
    }

    @Test
    void dropsRemindersOlderThanLateWindow() {
        // Due Nov 10 -> 8 days late, outside the 7-day window
        PurchaseRow tooLate = row(1L, 10L, 2).deliveredOn(LocalDate.of(2026, 9, 23)).build();
        // Due Nov 11 -> 7 days late, still inside
        PurchaseRow stillInside = row(2L, 11L, 2).productId(200L).deliveredOn(LocalDate.of(2026, 9, 24)).build();

        List<ReminderPlan> plans = selector.findDuePlans(List.of(tooLate, stillInside), Set.of(), TODAY);

        assertEquals(1, plans.size());
        assertEquals(List.of(200L), plans.get(0).items().stream().map(ReminderPlan.Item::productId).toList());
    }

    @Test
    void variantDurationOverridesProductDuration() {
        // Variant says 48 days per unit -> due on day 38 after Oct 11 = Nov 18
        PurchaseRow row = row(1L, 10L, 1).variantDays(48).deliveredOn(LocalDate.of(2026, 10, 11)).build();

        assertEquals(1, selector.findDuePlans(List.of(row), Set.of(), TODAY).size());
    }

    @Test
    void skipsProductsWithoutDuration() {
        PurchaseRow row = row(1L, 10L, 2).productDays(null).deliveredOn(LocalDate.of(2026, 10, 1)).build();

        assertTrue(selector.findDuePlans(List.of(row), Set.of(), TODAY).isEmpty());
    }

    @Test
    void fallsBackToShippedDateWhenNeverMarkedDelivered() {
        // Shipped Sep 28 + 3 fallback days = Oct 1 anchor
        PurchaseRow row = row(1L, 10L, 2).shippedOn(LocalDate.of(2026, 9, 28)).build();

        assertEquals(1, selector.findDuePlans(List.of(row), Set.of(), TODAY).size());
    }

    @Test
    void skipsWhenNeitherShippedNorDelivered() {
        PurchaseRow row = row(1L, 10L, 2).build();

        assertTrue(selector.findDuePlans(List.of(row), Set.of(), TODAY).isEmpty());
    }

    @Test
    void skipsWhenBuyerAlreadyRepurchasedTheProduct() {
        PurchaseRow original = row(1L, 10L, 2).deliveredOn(LocalDate.of(2026, 10, 1)).build();
        PurchaseRow repurchase = row(2L, 11L, 1)
                .orderedAt(LocalDateTime.of(2026, 11, 10, 9, 0))
                .deliveredOn(LocalDate.of(2026, 11, 12))
                .build();

        assertTrue(selector.findDuePlans(List.of(original, repurchase), Set.of(), TODAY).isEmpty());
    }

    @Test
    void skipsItemsAlreadyReminded() {
        PurchaseRow row = row(1L, 10L, 2).deliveredOn(LocalDate.of(2026, 10, 1)).build();

        assertTrue(selector.findDuePlans(List.of(row), Set.of(1L), TODAY).isEmpty());
    }

    @Test
    void skipsInactiveProductAndOutOfStockVariant() {
        PurchaseRow inactive = row(1L, 10L, 2).status(ProductStatus.INACTIVE)
                .deliveredOn(LocalDate.of(2026, 10, 1)).build();
        PurchaseRow noStock = row(2L, 11L, 2).productId(200L).stock(0)
                .deliveredOn(LocalDate.of(2026, 10, 1)).build();

        assertTrue(selector.findDuePlans(List.of(inactive, noStock), Set.of(), TODAY).isEmpty());
    }

    @Test
    void groupsAllDueProductsOfABuyerInOneEmailIgnoringEmailCase() {
        PurchaseRow first = row(1L, 10L, 2).deliveredOn(LocalDate.of(2026, 10, 1)).build();
        PurchaseRow second = row(2L, 11L, 2).productId(200L).email(" Buyer@Mail.com ")
                .deliveredOn(LocalDate.of(2026, 10, 1)).build();

        List<ReminderPlan> plans = selector.findDuePlans(List.of(first, second), Set.of(), TODAY);

        assertEquals(1, plans.size());
        assertEquals(2, plans.get(0).items().size());
    }

    @Test
    void sumsDurationOfSeveralVariantsOfSameProductInOneOrder() {
        // 1 x 30 days + 1 x 30 days in the same order = 60 days -> due Nov 18
        PurchaseRow a = row(1L, 10L, 1).deliveredOn(LocalDate.of(2026, 10, 1)).build();
        PurchaseRow b = row(2L, 10L, 1).variantId(51L).deliveredOn(LocalDate.of(2026, 10, 1)).build();

        List<ReminderPlan> plans = selector.findDuePlans(List.of(a, b), Set.of(), TODAY);

        assertEquals(1, plans.size());
        assertEquals(List.of(1L, 2L), plans.get(0).items().get(0).orderItemIds());
    }

    @Test
    void dropsUnsubscribedBuyers() {
        List<ReminderPlan> plans = duePlan();

        assertTrue(selector.applyRecipientRules(plans, Set.of(EMAIL), Map.of(), Map.of(), TODAY).isEmpty());
    }

    @Test
    void dropsBuyersEmailedWithinMinimumGap() {
        List<ReminderPlan> plans = duePlan();
        LocalDateTime lastOrder = LocalDateTime.of(2026, 11, 1, 0, 0);

        Map<String, Set<LocalDateTime>> sent29DaysAgo = Map.of(EMAIL, Set.of(TODAY.minusDays(29).atTime(10, 0)));
        Map<String, Set<LocalDateTime>> sent30DaysAgo = Map.of(EMAIL, Set.of(TODAY.minusDays(30).atTime(10, 0)));

        assertTrue(selector.applyRecipientRules(plans, Set.of(), sent29DaysAgo, Map.of(EMAIL, lastOrder), TODAY).isEmpty());
        assertEquals(1, selector.applyRecipientRules(plans, Set.of(), sent30DaysAgo, Map.of(EMAIL, lastOrder), TODAY).size());
    }

    @Test
    void pausesAfterTwoIgnoredRemindersUntilNextPurchase() {
        List<ReminderPlan> plans = duePlan();
        Map<String, Set<LocalDateTime>> twoReminders = Map.of(EMAIL, Set.of(
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 10, 0)));

        // Last purchase before both reminders: both were ignored -> paused
        Map<String, LocalDateTime> noPurchaseSince = Map.of(EMAIL, LocalDateTime.of(2026, 5, 1, 0, 0));
        assertTrue(selector.applyRecipientRules(plans, Set.of(), twoReminders, noPurchaseSince, TODAY).isEmpty());

        // Bought again after the reminders: counter resets
        Map<String, LocalDateTime> boughtAgain = Map.of(EMAIL, LocalDateTime.of(2026, 9, 1, 0, 0));
        assertEquals(1, selector.applyRecipientRules(plans, Set.of(), twoReminders, boughtAgain, TODAY).size());
    }

    private List<ReminderPlan> duePlan() {
        PurchaseRow row = row(1L, 10L, 2).deliveredOn(LocalDate.of(2026, 10, 1)).build();
        return selector.findDuePlans(List.of(row), Set.of(), TODAY);
    }

    private static RowBuilder row(Long itemId, Long orderId, int quantity) {
        return new RowBuilder(itemId, orderId, quantity);
    }

    private static final class RowBuilder {
        private final Long itemId;
        private final Long orderId;
        private final int quantity;
        private LocalDateTime orderedAt = LocalDateTime.of(2026, 9, 20, 9, 0);
        private String email = EMAIL;
        private Long variantId = 50L;
        private Integer variantDays;
        private Integer stock = 5;
        private Long productId = 100L;
        private Integer productDays = 30;
        private ProductStatus status = ProductStatus.ACTIVE;
        private LocalDateTime deliveredAt;
        private LocalDateTime shippedAt;

        private RowBuilder(Long itemId, Long orderId, int quantity) {
            this.itemId = itemId;
            this.orderId = orderId;
            this.quantity = quantity;
        }

        RowBuilder orderedAt(LocalDateTime value) { this.orderedAt = value; return this; }
        RowBuilder email(String value) { this.email = value; return this; }
        RowBuilder variantId(Long value) { this.variantId = value; return this; }
        RowBuilder variantDays(Integer value) { this.variantDays = value; return this; }
        RowBuilder stock(Integer value) { this.stock = value; return this; }
        RowBuilder productId(Long value) { this.productId = value; return this; }
        RowBuilder productDays(Integer value) { this.productDays = value; return this; }
        RowBuilder status(ProductStatus value) { this.status = value; return this; }
        RowBuilder deliveredOn(LocalDate value) { this.deliveredAt = value.atTime(15, 0); return this; }
        RowBuilder shippedOn(LocalDate value) { this.shippedAt = value.atTime(15, 0); return this; }

        PurchaseRow build() {
            return new PurchaseRow(itemId, orderId, orderedAt, email, null, "Ana", quantity,
                    variantId, variantDays, true, stock != null && stock > 0, new BigDecimal("25000"),
                    productId, "Tennis balls", productDays, status, deliveredAt, shippedAt);
        }
    }
}
