package com.forehapp.store.supplierSyncModule.application.usecases;

import com.forehapp.store.supplierSyncModule.application.SupplierSyncSettings;
import com.forehapp.store.supplierSyncModule.application.dto.SupplierCatalogIngestDto;
import com.forehapp.store.supplierSyncModule.domain.model.ItemSnapshot;
import com.forehapp.store.supplierSyncModule.domain.model.LinkSnapshot;
import com.forehapp.store.supplierSyncModule.domain.model.OrderAtRisk;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCatalogItem;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLink;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLinkStatus;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncConfig;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncEvent;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncRun;
import com.forehapp.store.supplierSyncModule.domain.model.SyncEventType;
import com.forehapp.store.supplierSyncModule.domain.model.SyncMode;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan.Action;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan.ActionType;
import com.forehapp.store.supplierSyncModule.domain.model.SyncPlan.EventDraft;
import com.forehapp.store.supplierSyncModule.domain.model.SyncRunStatus;
import com.forehapp.store.supplierSyncModule.domain.model.VariantRow;
import com.forehapp.store.supplierSyncModule.domain.ports.out.ISupplierSyncDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Transactional steps of an ingest; kept in their own bean so each one runs in its own transaction. */
@Component
public class SupplierSyncWriter {

    public record RunOutcome(SupplierSyncRun run, List<SupplierSyncEvent> events, int pendingSuggestions) {}

    private final ISupplierSyncDao dao;
    private final SupplierStockOperations stockOperations;
    private final SupplierSyncPlanner planner;

    public SupplierSyncWriter(ISupplierSyncDao dao, SupplierStockOperations stockOperations, SupplierSyncSettings settings) {
        this.dao = dao;
        this.stockOperations = stockOperations;
        this.planner = new SupplierSyncPlanner(settings.getMinMargin(), settings.getMaxDisableRatio(),
                settings.getMinDisableGuard(), settings.getDefaultRestock());
    }

    /** Upserts every supplier product by normalized name and stamps it as seen in this run. */
    @Transactional
    public void upsertCatalog(SupplierCode supplier, Map<String, SupplierCatalogIngestDto.Item> itemsByKey, LocalDateTime runTime) {
        Map<String, SupplierCatalogItem> existing = dao.findItems(supplier).stream()
                .collect(Collectors.toMap(SupplierCatalogItem::getNameKey, Function.identity(), (a, b) -> a));

        List<SupplierCatalogItem> toSave = new ArrayList<>();
        for (Map.Entry<String, SupplierCatalogIngestDto.Item> entry : itemsByKey.entrySet()) {
            SupplierCatalogIngestDto.Item dto = entry.getValue();
            SupplierCatalogItem item = existing.get(entry.getKey());
            if (item == null) {
                item = new SupplierCatalogItem();
                item.setSupplier(supplier);
                item.setNameKey(entry.getKey());
                item.setFirstSeenAt(runTime);
            }
            item.setName(dto.name().trim());
            item.setBrand(dto.brand());
            item.setCategory(dto.category());
            item.setPrice(dto.price());
            item.setOutOfStock(dto.outOfStock());
            item.setLastSeenAt(runTime);
            toSave.add(item);
        }
        dao.saveItems(toSave);
    }

    /**
     * Stores the script's best match for variants of enabled stores. A variant that is already confirmed or
     * marked as not supplied is skipped, and a pair the seller rejected is never suggested again.
     */
    @Transactional
    public int storeSuggestions(SupplierCode supplier, List<SupplierCatalogIngestDto.Suggestion> suggestions,
                                Set<Long> enabledStoreIds) {
        if (suggestions == null || suggestions.isEmpty() || enabledStoreIds.isEmpty()) return 0;

        Map<String, SupplierCatalogItem> itemsByKey = dao.findItems(supplier).stream()
                .collect(Collectors.toMap(SupplierCatalogItem::getNameKey, Function.identity(), (a, b) -> a));
        Set<Long> variantIds = suggestions.stream().map(SupplierCatalogIngestDto.Suggestion::variantId)
                .collect(Collectors.toSet());
        Map<Long, VariantRow> variants = dao.findVariantRows(variantIds).stream()
                .collect(Collectors.toMap(VariantRow::variantId, Function.identity()));
        Map<Long, List<SupplierLink>> linksByVariant = dao.findLinksForVariants(variantIds, supplier).stream()
                .collect(Collectors.groupingBy(SupplierLink::getVariantId));

        int stored = 0;
        for (SupplierCatalogIngestDto.Suggestion suggestion : suggestions) {
            VariantRow variant = variants.get(suggestion.variantId());
            SupplierCatalogItem item = itemsByKey.get(SupplierNames.normalize(suggestion.supplierName()));
            if (variant == null || item == null || !enabledStoreIds.contains(variant.storeId())) continue;

            List<SupplierLink> links = linksByVariant.computeIfAbsent(variant.variantId(), k -> new ArrayList<>());
            boolean settled = links.stream().anyMatch(l -> l.getStatus() == SupplierLinkStatus.CONFIRMED
                    || l.getStatus() == SupplierLinkStatus.NOT_SUPPLIED);
            boolean samePairExists = links.stream().anyMatch(l -> item.getId().equals(l.getSupplierItemId()));
            if (settled || samePairExists) continue;

            List<SupplierLink> replaced = links.stream()
                    .filter(l -> l.getStatus() == SupplierLinkStatus.SUGGESTED)
                    .toList();
            dao.deleteLinks(replaced);
            links.removeAll(replaced);

            SupplierLink link = new SupplierLink();
            link.setStoreId(variant.storeId());
            link.setVariantId(variant.variantId());
            link.setSupplier(supplier);
            link.setSupplierItemId(item.getId());
            link.setStatus(SupplierLinkStatus.SUGGESTED);
            link.setScore(suggestion.score());
            links.add(dao.saveLink(link));
            stored++;
        }
        return stored;
    }

    /** Plans and (in APPLY mode) executes the sync for one store, recording the run and its events. */
    @Transactional
    public RunOutcome runStore(SupplierSyncConfig config, LocalDateTime runTime, String supplierAbortReason,
                               int supplierItems, int supplierOutOfStock) {
        SupplierSyncRun run = new SupplierSyncRun();
        run.setStoreId(config.getStoreId());
        run.setSupplier(config.getSupplier());
        run.setMode(config.getMode());
        run.setStartedAt(LocalDateTime.now());
        run.setSupplierItems(supplierItems);
        run.setSupplierOutOfStock(supplierOutOfStock);

        if (supplierAbortReason != null) {
            run.setStatus(SyncRunStatus.ABORTED);
            run.setAbortReason(supplierAbortReason);
            run.setFinishedAt(LocalDateTime.now());
            return new RunOutcome(dao.saveRun(run), List.of(), pendingSuggestions(config));
        }

        List<LinkSnapshot> links = dao.findSnapshots(config.getStoreId(), config.getSupplier(),
                List.of(SupplierLinkStatus.CONFIRMED, SupplierLinkStatus.SUGGESTED));
        Map<Long, ItemSnapshot> items = loadItems(links, runTime);
        boolean guard = config.getMode() == SyncMode.APPLY && dao.hasAppliedRun(config.getStoreId(), config.getSupplier());

        SyncPlan plan = planner.plan(links, items, guard);
        run.setConfirmedLinks((int) links.stream().filter(l -> l.status() == SupplierLinkStatus.CONFIRMED).count());

        if (plan.aborted()) {
            run.setStatus(SyncRunStatus.ABORTED);
            run.setAbortReason(plan.abortReason());
        } else if (config.getMode() == SyncMode.APPLY) {
            execute(plan.actions(), config.getSupplier());
            run.setStatus(SyncRunStatus.APPLIED);
        } else {
            run.setStatus(SyncRunStatus.PREVIEW);
        }

        List<EventDraft> drafts = new ArrayList<>(plan.events());
        List<OrderAtRisk> orders = ordersAtRisk(plan.actions());
        drafts.addAll(orderEvents(orders, links));

        run.setDisabledCount((int) plan.count(SyncEventType.DISABLED));
        run.setReenabledCount((int) plan.count(SyncEventType.REENABLED));
        run.setCostUpdates((int) plan.count(SyncEventType.COST_UPDATED));
        run.setBrokenLinks((int) plan.count(SyncEventType.BROKEN_LINK));
        run.setMarginAlerts((int) plan.count(SyncEventType.MARGIN_ALERT));
        run.setOrdersAtRisk((int) orders.stream().map(OrderAtRisk::orderId).distinct().count());
        run.setFinishedAt(LocalDateTime.now());
        SupplierSyncRun saved = dao.saveRun(run);

        List<SupplierSyncEvent> events = drafts.stream().map(d -> toEvent(saved.getId(), d)).toList();
        dao.saveEvents(events);
        return new RunOutcome(saved, events, pendingSuggestions(config));
    }

    private Map<Long, ItemSnapshot> loadItems(List<LinkSnapshot> links, LocalDateTime runTime) {
        Set<Long> itemIds = links.stream().map(LinkSnapshot::supplierItemId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, ItemSnapshot> items = new HashMap<>();
        for (SupplierCatalogItem item : dao.findItemsByIds(itemIds)) {
            items.put(item.getId(), new ItemSnapshot(item.getId(), item.getName(), item.getPrice(),
                    Boolean.TRUE.equals(item.getOutOfStock()), !item.getLastSeenAt().isBefore(runTime)));
        }
        return items;
    }

    private void execute(List<Action> actions, SupplierCode supplier) {
        if (actions.isEmpty()) return;

        Set<Long> productIds = new HashSet<>();
        for (Action action : actions) {
            SupplierLink link = dao.findLink(action.linkId()).orElse(null);
            if (link == null) continue;

            switch (action.type()) {
                case DISABLE -> {
                    int previous = stockOperations.zeroStock(action.variantId());
                    if (previous > 0) {
                        link.setDisabledBySync(true);
                        link.setStockBeforeSync(previous);
                        productIds.add(action.productId());
                    }
                }
                case REENABLE -> {
                    stockOperations.restoreStock(action.variantId(), action.stock());
                    link.setDisabledBySync(false);
                    link.setStockBeforeSync(null);
                    productIds.add(action.productId());
                }
                case RELEASE -> {
                    link.setDisabledBySync(false);
                    link.setStockBeforeSync(null);
                }
                case UPDATE_COST -> stockOperations.updateCost(action.variantId(), action.cost(),
                        "Supplier sync (" + supplier.name() + ")");
            }
            dao.saveLink(link);
        }
        stockOperations.syncProductStatus(productIds);
        stockOperations.evictCatalogCaches();
    }

    private List<OrderAtRisk> ordersAtRisk(List<Action> actions) {
        Set<Long> disabledVariants = actions.stream()
                .filter(a -> a.type() == ActionType.DISABLE)
                .map(Action::variantId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return dao.findOpenOrdersWithVariants(disabledVariants);
    }

    private List<EventDraft> orderEvents(List<OrderAtRisk> orders, Collection<LinkSnapshot> links) {
        Map<Long, LinkSnapshot> byVariant = links.stream()
                .collect(Collectors.toMap(LinkSnapshot::variantId, Function.identity(), (a, b) -> a));
        return orders.stream().map(o -> {
            LinkSnapshot link = byVariant.get(o.variantId());
            return new EventDraft(SyncEventType.ORDER_AT_RISK, o.variantId(),
                    link == null ? null : link.productId(),
                    link == null ? null : link.productTitle(),
                    link == null ? null : link.variantLabel(),
                    null, String.valueOf(o.orderId()), String.valueOf(o.quantity()),
                    "Open order with a product now out of stock at the supplier", false);
        }).toList();
    }

    private int pendingSuggestions(SupplierSyncConfig config) {
        return dao.findLinks(config.getStoreId(), config.getSupplier(), SupplierLinkStatus.SUGGESTED).size();
    }

    private static SupplierSyncEvent toEvent(Long runId, EventDraft d) {
        SupplierSyncEvent e = new SupplierSyncEvent();
        e.setRunId(runId);
        e.setType(d.type());
        e.setVariantId(d.variantId());
        e.setProductId(d.productId());
        e.setProductTitle(truncate(d.productTitle(), 255));
        e.setVariantLabel(truncate(d.variantLabel(), 255));
        e.setSupplierItemName(truncate(d.supplierItemName(), 255));
        e.setOldValue(truncate(d.oldValue(), 100));
        e.setNewValue(truncate(d.newValue(), 100));
        e.setDetail(truncate(d.detail(), 255));
        e.setUnconfirmed(d.unconfirmed());
        return e;
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
