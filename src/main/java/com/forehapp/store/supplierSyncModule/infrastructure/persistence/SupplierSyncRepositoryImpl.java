package com.forehapp.store.supplierSyncModule.infrastructure.persistence;

import com.forehapp.store.supplierSyncModule.domain.model.LinkSnapshot;
import com.forehapp.store.supplierSyncModule.domain.model.OrderAtRisk;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCatalogItem;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLink;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLinkStatus;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncConfig;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncEvent;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncRun;
import com.forehapp.store.supplierSyncModule.domain.model.SyncRunStatus;
import com.forehapp.store.supplierSyncModule.domain.model.VariantRow;
import com.forehapp.store.supplierSyncModule.domain.ports.out.ISupplierSyncDao;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SupplierSyncRepositoryImpl implements ISupplierSyncDao {

    // Statuses that take a variant out of the "still to pair" list
    private static final List<SupplierLinkStatus> PAIRED_STATUSES =
            List.of(SupplierLinkStatus.CONFIRMED, SupplierLinkStatus.SUGGESTED, SupplierLinkStatus.NOT_SUPPLIED);

    private final ISupplierSyncConfigJpaRepository configRepository;
    private final ISupplierCatalogItemJpaRepository itemRepository;
    private final ISupplierLinkJpaRepository linkRepository;
    private final ISupplierSyncRunJpaRepository runRepository;
    private final ISupplierSyncEventJpaRepository eventRepository;

    public SupplierSyncRepositoryImpl(ISupplierSyncConfigJpaRepository configRepository,
                                      ISupplierCatalogItemJpaRepository itemRepository,
                                      ISupplierLinkJpaRepository linkRepository,
                                      ISupplierSyncRunJpaRepository runRepository,
                                      ISupplierSyncEventJpaRepository eventRepository) {
        this.configRepository = configRepository;
        this.itemRepository = itemRepository;
        this.linkRepository = linkRepository;
        this.runRepository = runRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public Optional<SupplierSyncConfig> findConfig(Long storeId, SupplierCode supplier) {
        return configRepository.findByStoreIdAndSupplier(storeId, supplier);
    }

    @Override
    public List<SupplierSyncConfig> findEnabledConfigs(SupplierCode supplier) {
        return configRepository.findBySupplierAndEnabledTrue(supplier);
    }

    @Override
    public SupplierSyncConfig saveConfig(SupplierSyncConfig config) {
        return configRepository.save(config);
    }

    @Override
    public List<SupplierCatalogItem> findItems(SupplierCode supplier) {
        return itemRepository.findBySupplier(supplier);
    }

    @Override
    public List<SupplierCatalogItem> findItemsByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return itemRepository.findByIdIn(ids);
    }

    @Override
    public Optional<SupplierCatalogItem> findItem(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public List<SupplierCatalogItem> searchItems(SupplierCode supplier, String nameKeyFragment, int limit) {
        return itemRepository.search(supplier, nameKeyFragment, PageRequest.of(0, limit));
    }

    @Override
    public void saveItems(Collection<SupplierCatalogItem> items) {
        itemRepository.saveAll(items);
    }

    @Override
    public Optional<SupplierLink> findLink(Long linkId) {
        return linkRepository.findById(linkId);
    }

    @Override
    public List<SupplierLink> findLinksForVariant(Long variantId, SupplierCode supplier) {
        return linkRepository.findByVariantIdAndSupplier(variantId, supplier);
    }

    @Override
    public List<SupplierLink> findLinksForVariants(Collection<Long> variantIds, SupplierCode supplier) {
        if (variantIds.isEmpty()) return List.of();
        return linkRepository.findByVariantIdInAndSupplier(variantIds, supplier);
    }

    @Override
    public List<SupplierLink> findLinks(Long storeId, SupplierCode supplier, SupplierLinkStatus status) {
        return linkRepository.findByStoreAndStatus(storeId, supplier, status);
    }

    @Override
    public List<LinkSnapshot> findSnapshots(Long storeId, SupplierCode supplier, Collection<SupplierLinkStatus> statuses) {
        return linkRepository.findSnapshots(storeId, supplier, statuses);
    }

    @Override
    public SupplierLink saveLink(SupplierLink link) {
        return linkRepository.save(link);
    }

    @Override
    public void saveLinks(Collection<SupplierLink> links) {
        linkRepository.saveAll(links);
    }

    @Override
    public void deleteLinks(Collection<SupplierLink> links) {
        linkRepository.deleteAll(links);
    }

    @Override
    public List<VariantRow> findVariantsWithoutLink(Collection<Long> storeIds, SupplierCode supplier) {
        if (storeIds.isEmpty()) return List.of();
        return linkRepository.findVariantsWithoutLink(storeIds, supplier, PAIRED_STATUSES);
    }

    @Override
    public List<VariantRow> findVariantRows(Collection<Long> variantIds) {
        if (variantIds.isEmpty()) return List.of();
        return linkRepository.findVariantRows(variantIds);
    }

    @Override
    public Map<Long, List<String[]>> findVariantAttributes(Collection<Long> variantIds) {
        Map<Long, List<String[]>> result = new HashMap<>();
        if (variantIds.isEmpty()) return result;
        for (Object[] row : linkRepository.findVariantAttributes(variantIds)) {
            result.computeIfAbsent((Long) row[0], k -> new ArrayList<>())
                    .add(new String[]{(String) row[1], (String) row[2]});
        }
        return result;
    }

    @Override
    public List<OrderAtRisk> findOpenOrdersWithVariants(Collection<Long> variantIds) {
        if (variantIds.isEmpty()) return List.of();
        return linkRepository.findOpenOrdersWithVariants(variantIds);
    }

    @Override
    public SupplierSyncRun saveRun(SupplierSyncRun run) {
        return runRepository.save(run);
    }

    @Override
    public Optional<SupplierSyncRun> findRun(Long runId, Long storeId) {
        return runRepository.findByIdAndStoreId(runId, storeId);
    }

    @Override
    public List<SupplierSyncRun> findRuns(Long storeId, SupplierCode supplier, int limit) {
        return runRepository.findByStoreIdAndSupplierOrderByStartedAtDesc(storeId, supplier, PageRequest.of(0, limit));
    }

    @Override
    public boolean hasAppliedRun(Long storeId, SupplierCode supplier) {
        return runRepository.existsByStoreIdAndSupplierAndStatus(storeId, supplier, SyncRunStatus.APPLIED);
    }

    @Override
    public Optional<Integer> findLastAcceptedCatalogSize(SupplierCode supplier) {
        return runRepository.findAcceptedCatalogSizes(supplier, PageRequest.of(0, 1)).stream().findFirst();
    }

    @Override
    public void saveEvents(Collection<SupplierSyncEvent> events) {
        eventRepository.saveAll(events);
    }

    @Override
    public List<SupplierSyncEvent> findEvents(Long runId) {
        return eventRepository.findByRunIdOrderByTypeAscIdAsc(runId);
    }
}
