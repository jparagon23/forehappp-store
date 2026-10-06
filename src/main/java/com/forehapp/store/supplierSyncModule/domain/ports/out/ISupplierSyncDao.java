package com.forehapp.store.supplierSyncModule.domain.ports.out;

import com.forehapp.store.supplierSyncModule.domain.model.LinkSnapshot;
import com.forehapp.store.supplierSyncModule.domain.model.OrderAtRisk;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCatalogItem;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLink;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLinkStatus;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncConfig;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncEvent;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncRun;
import com.forehapp.store.supplierSyncModule.domain.model.VariantRow;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ISupplierSyncDao {

    Optional<SupplierSyncConfig> findConfig(Long storeId, SupplierCode supplier);
    List<SupplierSyncConfig> findEnabledConfigs(SupplierCode supplier);
    SupplierSyncConfig saveConfig(SupplierSyncConfig config);

    List<SupplierCatalogItem> findItems(SupplierCode supplier);
    List<SupplierCatalogItem> findItemsByIds(Collection<Long> ids);
    Optional<SupplierCatalogItem> findItem(Long id);
    List<SupplierCatalogItem> searchItems(SupplierCode supplier, String nameKeyFragment, int limit);
    void saveItems(Collection<SupplierCatalogItem> items);

    Optional<SupplierLink> findLink(Long linkId);
    List<SupplierLink> findLinksForVariant(Long variantId, SupplierCode supplier);
    List<SupplierLink> findLinksForVariants(Collection<Long> variantIds, SupplierCode supplier);
    List<SupplierLink> findLinks(Long storeId, SupplierCode supplier, SupplierLinkStatus status);
    List<LinkSnapshot> findSnapshots(Long storeId, SupplierCode supplier, Collection<SupplierLinkStatus> statuses);
    SupplierLink saveLink(SupplierLink link);
    void saveLinks(Collection<SupplierLink> links);
    void deleteLinks(Collection<SupplierLink> links);

    List<VariantRow> findVariantsWithoutLink(Collection<Long> storeIds, SupplierCode supplier);
    List<VariantRow> findVariantRows(Collection<Long> variantIds);
    /** variantId → "Attribute: value" pairs, in no particular order. */
    Map<Long, List<String[]>> findVariantAttributes(Collection<Long> variantIds);

    List<OrderAtRisk> findOpenOrdersWithVariants(Collection<Long> variantIds);

    SupplierSyncRun saveRun(SupplierSyncRun run);
    Optional<SupplierSyncRun> findRun(Long runId, Long storeId);
    List<SupplierSyncRun> findRuns(Long storeId, SupplierCode supplier, int limit);
    boolean hasAppliedRun(Long storeId, SupplierCode supplier);
    Optional<Integer> findLastAcceptedCatalogSize(SupplierCode supplier);

    void saveEvents(Collection<SupplierSyncEvent> events);
    List<SupplierSyncEvent> findEvents(Long runId);
}
