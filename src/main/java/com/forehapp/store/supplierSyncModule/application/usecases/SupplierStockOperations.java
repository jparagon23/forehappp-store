package com.forehapp.store.supplierSyncModule.application.usecases;

import com.forehapp.store.productModule.domain.model.InventoryMovement;
import com.forehapp.store.productModule.domain.model.MovementReason;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.forehapp.store.productModule.domain.model.VariantCostHistory;
import com.forehapp.store.productModule.domain.ports.out.IInventoryMovementDao;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import com.forehapp.store.productModule.domain.ports.out.IVariantCostHistoryDao;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/** Stock and cost changes made by the supplier sync. Callers provide the transaction. */
@Component
public class SupplierStockOperations {

    private static final List<String> CATALOG_CACHES = List.of(
            "public-products", "public-product-brand-facets", "discovery-sections",
            "seller-products", "seller-product-detail", "wishlist");

    private final IProductVariantDao variantDao;
    private final IInventoryMovementDao movementDao;
    private final IProductDao productDao;
    private final IVariantCostHistoryDao costHistoryDao;
    private final CacheManager cacheManager;

    public SupplierStockOperations(IProductVariantDao variantDao,
                                   IInventoryMovementDao movementDao,
                                   IProductDao productDao,
                                   IVariantCostHistoryDao costHistoryDao,
                                   CacheManager cacheManager) {
        this.variantDao = variantDao;
        this.movementDao = movementDao;
        this.productDao = productDao;
        this.costHistoryDao = costHistoryDao;
        this.cacheManager = cacheManager;
    }

    /** Sets the variant stock to 0 and returns the stock it had (0 when there was nothing to remove). */
    public int zeroStock(Long variantId) {
        ProductVariant variant = variantDao.findByIdForUpdate(variantId).orElse(null);
        if (variant == null || variant.getStock() <= 0) return 0;

        int previous = variant.getStock();
        recordMovement(variant, -previous);
        variant.setStock(0);
        variantDao.save(variant);
        return previous;
    }

    /** Restores stock on a variant still at 0; a variant someone already restocked is left as is. */
    public boolean restoreStock(Long variantId, int quantity) {
        ProductVariant variant = variantDao.findByIdForUpdate(variantId).orElse(null);
        if (variant == null || variant.getStock() != 0 || quantity <= 0) return false;

        recordMovement(variant, quantity);
        variant.setStock(quantity);
        variantDao.save(variant);
        return true;
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

    /** Same rule as manual inventory adjustments: all variants at 0 → OUT_OF_STOCK, back to ACTIVE when one has stock. */
    public void syncProductStatus(Collection<Long> productIds) {
        for (Long productId : productIds) {
            Product product = productDao.findById(productId).orElse(null);
            if (product == null) continue;

            boolean allEmpty = product.getVariants().stream().allMatch(v -> v.getStock() == 0);
            if (allEmpty && product.getStatus() == ProductStatus.ACTIVE) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
                productDao.save(product);
            } else if (!allEmpty && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
                product.setStatus(ProductStatus.ACTIVE);
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

    private void recordMovement(ProductVariant variant, int quantity) {
        InventoryMovement movement = new InventoryMovement();
        movement.setVariant(variant);
        movement.setQuantity(quantity);
        movement.setReason(MovementReason.SUPPLIER_SYNC);
        movementDao.save(movement);
    }
}
