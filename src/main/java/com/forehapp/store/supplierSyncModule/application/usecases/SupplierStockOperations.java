package com.forehapp.store.supplierSyncModule.application.usecases;

import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.forehapp.store.productModule.domain.model.VariantCostHistory;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import com.forehapp.store.productModule.domain.ports.out.IVariantCostHistoryDao;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/** Availability and cost changes made by the supplier sync. Callers provide the transaction. */
@Component
public class SupplierStockOperations {

    private static final List<String> CATALOG_CACHES = List.of(
            "public-products", "public-product-brand-facets", "discovery-sections",
            "seller-products", "seller-product-detail", "wishlist");

    private final IProductVariantDao variantDao;
    private final IProductDao productDao;
    private final IVariantCostHistoryDao costHistoryDao;
    private final CacheManager cacheManager;

    public SupplierStockOperations(IProductVariantDao variantDao,
                                   IProductDao productDao,
                                   IVariantCostHistoryDao costHistoryDao,
                                   CacheManager cacheManager) {
        this.variantDao = variantDao;
        this.productDao = productDao;
        this.costHistoryDao = costHistoryDao;
        this.cacheManager = cacheManager;
    }

    /** Records whether the supplier has the variant now. Returns true when it changed. */
    public boolean setSupplierAvailable(Long variantId, boolean available) {
        ProductVariant variant = variantDao.findByIdForUpdate(variantId).orElse(null);
        if (variant == null || Boolean.valueOf(available).equals(variant.getSupplierAvailable())) return false;

        variant.setSupplierAvailable(available);
        variantDao.save(variant);
        return true;
    }

    /** A confirmed pair means the supplier ships the variant: it becomes dropship with the supplier's availability. */
    public void markSupplied(Long variantId, boolean available) {
        ProductVariant variant = variantDao.findByIdForUpdate(variantId).orElse(null);
        if (variant == null) return;

        variant.setDropship(true);
        variant.setSupplierAvailable(available);
        variantDao.save(variant);
        syncProductStatus(List.of(variant.getProduct().getId()));
        evictCatalogCaches();
    }

    public void updateCost(Long variantId, BigDecimal cost, String note) {
        ProductVariant variant = variantDao.findById(variantId).orElse(null);
        if (variant == null || (variant.getCost() != null && variant.getCost().compareTo(cost) == 0)) return;

        variant.setCost(cost);
        variantDao.save(variant);

        VariantCostHistory history = new VariantCostHistory();
        history.setVariant(variant);
        history.setCost(cost);
        history.setNotes(note);
        costHistoryDao.save(history);
    }

    /** Same rule as everywhere else: nothing sellable → OUT_OF_STOCK, back to ACTIVE when something is. */
    public void syncProductStatus(Collection<Long> productIds) {
        for (Long productId : productIds) {
            Product product = productDao.findById(productId).orElse(null);
            if (product != null && product.refreshStockStatus()) {
                productDao.save(product);
            }
        }
    }

    public void evictCatalogCaches() {
        for (String name : CATALOG_CACHES) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        }
    }

}
