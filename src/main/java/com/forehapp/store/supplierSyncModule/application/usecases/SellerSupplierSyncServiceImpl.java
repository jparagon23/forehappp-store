package com.forehapp.store.supplierSyncModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import com.forehapp.store.supplierSyncModule.application.SupplierSyncSettings;
import com.forehapp.store.supplierSyncModule.application.dto.SupplierItemResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SupplierLinkResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SyncConfigDto;
import com.forehapp.store.supplierSyncModule.application.dto.SyncEventResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SyncRunResponse;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCatalogItem;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLink;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierLinkStatus;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncConfig;
import com.forehapp.store.supplierSyncModule.domain.model.SyncMode;
import com.forehapp.store.supplierSyncModule.domain.model.VariantRow;
import com.forehapp.store.supplierSyncModule.domain.ports.in.ISellerSupplierSyncService;
import com.forehapp.store.supplierSyncModule.domain.ports.out.ISupplierSyncDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SellerSupplierSyncServiceImpl implements ISellerSupplierSyncService {

    private static final String UNLINKED = "UNLINKED";
    private static final int SEARCH_LIMIT = 30;

    private final ISupplierSyncDao dao;
    private final IStoreMembershipDao membershipDao;
    private final SupplierStockOperations stockOperations;
    private final SupplierSyncSettings settings;

    public SellerSupplierSyncServiceImpl(ISupplierSyncDao dao,
                                         IStoreMembershipDao membershipDao,
                                         SupplierStockOperations stockOperations,
                                         SupplierSyncSettings settings) {
        this.dao = dao;
        this.membershipDao = membershipDao;
        this.stockOperations = stockOperations;
        this.settings = settings;
    }

    // ── Config ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public SyncConfigDto getConfig(Long storeId, SupplierCode supplier, Long userId) {
        requireStoreAccess(storeId, userId);
        return dao.findConfig(storeId, supplier)
                .map(c -> new SyncConfigDto(supplier.name(), c.getEnabled(), c.getMode()))
                .orElse(new SyncConfigDto(supplier.name(), false, SyncMode.PREVIEW));
    }

    @Override
    @Transactional
    public SyncConfigDto updateConfig(Long storeId, SupplierCode supplier, SyncConfigDto dto, Long userId) {
        requireStoreAccess(storeId, userId);
        SupplierSyncConfig config = dao.findConfig(storeId, supplier).orElseGet(() -> {
            SupplierSyncConfig c = new SupplierSyncConfig();
            c.setStoreId(storeId);
            c.setSupplier(supplier);
            return c;
        });
        config.setEnabled(dto.enabled());
        config.setMode(dto.mode());
        SupplierSyncConfig saved = dao.saveConfig(config);
        return new SyncConfigDto(supplier.name(), saved.getEnabled(), saved.getMode());
    }

    // ── Pairs ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SupplierLinkResponse> listLinks(Long storeId, SupplierCode supplier, String filter, Long userId) {
        requireStoreAccess(storeId, userId);

        if (filter == null || UNLINKED.equalsIgnoreCase(filter)) {
            List<VariantRow> variants = dao.findVariantsWithoutLink(Set.of(storeId), supplier);
            Map<Long, List<String[]>> attributes = dao.findVariantAttributes(ids(variants));
            return variants.stream().map(v -> toResponse(null, v, null, attributes)).toList();
        }

        SupplierLinkStatus status = parseStatus(filter);
        List<SupplierLink> links = dao.findLinks(storeId, supplier, status);
        return toResponses(links);
    }

    @Override
    @Transactional
    public List<SupplierLinkResponse> confirmLinks(Long storeId, SupplierCode supplier, List<Long> linkIds, Long userId) {
        requireStoreAccess(storeId, userId);
        List<SupplierLink> confirmed = new ArrayList<>();
        for (Long linkId : linkIds) {
            SupplierLink link = requireLink(storeId, supplier, linkId);
            if (link.getStatus() == SupplierLinkStatus.CONFIRMED) {
                confirmed.add(link);
                continue;
            }
            if (link.getSupplierItemId() == null
                    || (link.getStatus() != SupplierLinkStatus.SUGGESTED && link.getStatus() != SupplierLinkStatus.REJECTED)) {
                throw new BadRequestException(ErrorCode.SUPPLIER_LINK_INVALID_STATUS,
                        "Only suggested or rejected pairs can be confirmed (link " + linkId + ")");
            }
            confirmed.add(confirmPair(storeId, supplier, link.getVariantId(), link.getSupplierItemId(), link.getScore(), userId));
        }
        return toResponses(confirmed);
    }

    @Override
    @Transactional
    public void rejectLink(Long storeId, SupplierCode supplier, Long linkId, Long userId) {
        requireStoreAccess(storeId, userId);
        SupplierLink link = requireLink(storeId, supplier, linkId);
        if (link.getStatus() != SupplierLinkStatus.SUGGESTED && link.getStatus() != SupplierLinkStatus.CONFIRMED) {
            throw new BadRequestException(ErrorCode.SUPPLIER_LINK_INVALID_STATUS, "Only suggested or confirmed pairs can be rejected");
        }
        releaseStock(List.of(link));
        link.setStatus(SupplierLinkStatus.REJECTED);
        link.setDisabledBySync(false);
        link.setStockBeforeSync(null);
        dao.saveLink(link);
    }

    @Override
    @Transactional
    public SupplierLinkResponse linkManually(Long storeId, SupplierCode supplier, Long variantId, Long supplierItemId, Long userId) {
        requireStoreAccess(storeId, userId);
        requireVariant(storeId, variantId);
        SupplierCatalogItem item = dao.findItem(supplierItemId)
                .filter(i -> i.getSupplier() == supplier)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SUPPLIER_ITEM_NOT_FOUND, "Supplier product not found"));

        SupplierLink link = confirmPair(storeId, supplier, variantId, item.getId(), null, userId);
        return toResponses(List.of(link)).get(0);
    }

    @Override
    @Transactional
    public void markNotSupplied(Long storeId, SupplierCode supplier, Long variantId, Long userId) {
        requireStoreAccess(storeId, userId);
        requireVariant(storeId, variantId);
        removeActiveLinks(supplier, variantId);

        SupplierLink link = new SupplierLink();
        link.setStoreId(storeId);
        link.setVariantId(variantId);
        link.setSupplier(supplier);
        link.setStatus(SupplierLinkStatus.NOT_SUPPLIED);
        link.setConfirmedByUserId(userId);
        link.setConfirmedAt(LocalDateTime.now());
        dao.saveLink(link);
    }

    @Override
    @Transactional
    public void unlink(Long storeId, SupplierCode supplier, Long variantId, Long userId) {
        requireStoreAccess(storeId, userId);
        requireVariant(storeId, variantId);
        removeActiveLinks(supplier, variantId);
    }

    // ── Catalog & history ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SupplierItemResponse> searchItems(Long storeId, SupplierCode supplier, String query, Long userId) {
        requireStoreAccess(storeId, userId);
        return dao.searchItems(supplier, SupplierNames.normalize(query), SEARCH_LIMIT).stream()
                .map(SupplierItemResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncRunResponse> listRuns(Long storeId, SupplierCode supplier, int limit, Long userId) {
        requireStoreAccess(storeId, userId);
        return dao.findRuns(storeId, supplier, Math.max(1, Math.min(limit, 50))).stream()
                .map(SyncRunResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncEventResponse> listRunEvents(Long storeId, Long runId, Long userId) {
        requireStoreAccess(storeId, userId);
        dao.findRun(runId, storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SUPPLIER_SYNC_RUN_NOT_FOUND, "Sync run not found"));
        return dao.findEvents(runId).stream().map(SyncEventResponse::from).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────

    /**
     * Makes (variant, item) the variant's only active pair. Rejected pairs are kept so they are not
     * suggested again; a previous confirmed pair hands over its sync state, since the variant stock
     * it set to 0 is still in place.
     */
    private SupplierLink confirmPair(Long storeId, SupplierCode supplier, Long variantId, Long itemId,
                                     BigDecimal score, Long userId) {
        List<SupplierLink> links = dao.findLinksForVariant(variantId, supplier);
        SupplierLink target = links.stream()
                .filter(l -> itemId.equals(l.getSupplierItemId()))
                .findFirst()
                .orElseGet(SupplierLink::new);

        SupplierLink previousConfirmed = links.stream()
                .filter(l -> l != target && l.getStatus() == SupplierLinkStatus.CONFIRMED)
                .findFirst()
                .orElse(null);
        if (previousConfirmed != null && Boolean.TRUE.equals(previousConfirmed.getDisabledBySync())) {
            target.setDisabledBySync(true);
            target.setStockBeforeSync(previousConfirmed.getStockBeforeSync());
        }

        dao.deleteLinks(links.stream()
                .filter(l -> l != target && l.getStatus() != SupplierLinkStatus.REJECTED)
                .toList());

        target.setStoreId(storeId);
        target.setVariantId(variantId);
        target.setSupplier(supplier);
        target.setSupplierItemId(itemId);
        target.setStatus(SupplierLinkStatus.CONFIRMED);
        if (target.getScore() == null) target.setScore(score);
        target.setConfirmedByUserId(userId);
        target.setConfirmedAt(LocalDateTime.now());
        return dao.saveLink(target);
    }

    /** Deletes the variant's confirmed, suggested and not-supplied pairs, giving back stock the sync had removed. */
    private void removeActiveLinks(SupplierCode supplier, Long variantId) {
        List<SupplierLink> active = dao.findLinksForVariant(variantId, supplier).stream()
                .filter(l -> l.getStatus() != SupplierLinkStatus.REJECTED)
                .toList();
        releaseStock(active);
        dao.deleteLinks(active);
    }

    private void releaseStock(Collection<SupplierLink> links) {
        List<SupplierLink> held = links.stream()
                .filter(l -> l.getStatus() == SupplierLinkStatus.CONFIRMED && Boolean.TRUE.equals(l.getDisabledBySync()))
                .toList();
        if (held.isEmpty()) return;

        for (SupplierLink link : held) {
            int restore = link.getStockBeforeSync() != null && link.getStockBeforeSync() > 0
                    ? link.getStockBeforeSync()
                    : settings.getDefaultRestock();
            stockOperations.restoreStock(link.getVariantId(), restore);
        }
        Set<Long> productIds = dao.findVariantRows(held.stream().map(SupplierLink::getVariantId).toList()).stream()
                .map(VariantRow::productId)
                .collect(Collectors.toSet());
        stockOperations.syncProductStatus(productIds);
        stockOperations.evictCatalogCaches();
    }

    private List<SupplierLinkResponse> toResponses(List<SupplierLink> links) {
        Set<Long> variantIds = links.stream().map(SupplierLink::getVariantId).collect(Collectors.toSet());
        Map<Long, VariantRow> variants = dao.findVariantRows(variantIds).stream()
                .collect(Collectors.toMap(VariantRow::variantId, Function.identity()));
        Map<Long, SupplierCatalogItem> items = dao.findItemsByIds(links.stream()
                        .map(SupplierLink::getSupplierItemId).filter(Objects::nonNull).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(SupplierCatalogItem::getId, Function.identity()));
        Map<Long, List<String[]>> attributes = dao.findVariantAttributes(variantIds);

        return links.stream()
                .filter(l -> variants.containsKey(l.getVariantId()))
                .map(l -> toResponse(l, variants.get(l.getVariantId()), items.get(l.getSupplierItemId()), attributes))
                .toList();
    }

    private SupplierLinkResponse toResponse(SupplierLink link, VariantRow v, SupplierCatalogItem item,
                                            Map<Long, List<String[]>> attributes) {
        String attributeLabel = attributes.getOrDefault(v.variantId(), List.of()).stream()
                .map(a -> a[0] + ": " + a[1])
                .collect(Collectors.joining(" · "));
        SupplierLinkResponse.Variant variant = new SupplierLinkResponse.Variant(
                v.variantId(), v.productId(), v.productTitle(), v.brand(), v.sku(),
                attributeLabel.isEmpty() ? null : attributeLabel,
                Boolean.TRUE.equals(v.active()), v.stock() == null ? 0 : v.stock(), v.price());

        BigDecimal margin = null;
        if (item != null && item.getPrice() != null && v.price() != null && v.price().signum() > 0) {
            margin = v.price().subtract(item.getPrice())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(v.price(), 2, RoundingMode.HALF_UP);
        }

        return new SupplierLinkResponse(
                link == null ? null : link.getId(),
                link == null ? null : link.getStatus().name(),
                link == null ? null : link.getScore(),
                link != null && Boolean.TRUE.equals(link.getDisabledBySync()),
                variant,
                item == null ? null : SupplierItemResponse.from(item),
                margin);
    }

    private SupplierLinkStatus parseStatus(String filter) {
        try {
            return SupplierLinkStatus.valueOf(filter.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorCode.SUPPLIER_LINK_INVALID_STATUS, "Unknown status filter: " + filter);
        }
    }

    private SupplierLink requireLink(Long storeId, SupplierCode supplier, Long linkId) {
        return dao.findLink(linkId)
                .filter(l -> l.getStoreId().equals(storeId) && l.getSupplier() == supplier)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SUPPLIER_LINK_NOT_FOUND, "Supplier pair not found"));
    }

    private void requireVariant(Long storeId, Long variantId) {
        boolean owned = dao.findVariantRows(List.of(variantId)).stream().anyMatch(v -> v.storeId().equals(storeId));
        if (!owned) {
            throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Variant not found");
        }
    }

    private void requireStoreAccess(Long storeId, Long userId) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .filter(m -> m.getRole() != StoreMemberRole.STAFF)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED,
                        "You do not have permission to manage this store's supplier sync"));
    }

    private static List<Long> ids(List<VariantRow> variants) {
        return variants.stream().map(VariantRow::variantId).toList();
    }
}
