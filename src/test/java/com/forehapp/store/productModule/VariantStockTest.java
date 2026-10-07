package com.forehapp.store.productModule;

import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariantStockTest {

    @Test
    void ownStockOnlySellsWhatTheStoreHolds() {
        ProductVariant v = variant(3, false, true);

        assertTrue(v.canFulfill(3));
        assertFalse(v.canFulfill(4));
        assertEquals(3, v.maxQuantity());
        assertEquals(0, v.consume(2));
        assertEquals(1, v.getStock());
    }

    @Test
    void dropshipTakesOwnStockFirstAndTheRestFromTheSupplier() {
        ProductVariant v = variant(2, true, true);

        assertNull(v.maxQuantity());
        assertEquals(3, v.consume(5));
        assertEquals(0, v.getStock());
        assertTrue(v.isSellable());
    }

    @Test
    void dropshipWithSupplierOutSellsOnlyOwnStock() {
        ProductVariant v = variant(1, true, false);

        assertTrue(v.isSellable());
        assertFalse(v.canFulfill(2));
        assertEquals(0, v.consume(1));
        assertFalse(v.isSellable());
        assertThrows(IllegalStateException.class, () -> v.consume(1));
    }

    @Test
    void supplierAvailabilityIsIgnoredWhenNotDropship() {
        assertFalse(variant(0, false, true).isSellable());
    }

    @Test
    void inactiveVariantIsNeverSellable() {
        ProductVariant v = variant(5, true, true);
        v.setActive(false);

        assertFalse(v.isSellable());
    }

    @Test
    void productStatusFollowsWhetherAnythingCanBeSold() {
        Product product = new Product();
        ProductVariant v = variant(0, true, true);
        product.getVariants().add(v);

        product.setStatus(ProductStatus.OUT_OF_STOCK);
        assertTrue(product.refreshStockStatus());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());

        v.setSupplierAvailable(false);
        assertTrue(product.refreshStockStatus());
        assertEquals(ProductStatus.OUT_OF_STOCK, product.getStatus());

        product.setStatus(ProductStatus.DRAFT);
        v.setSupplierAvailable(true);
        assertFalse(product.refreshStockStatus());
        assertEquals(ProductStatus.DRAFT, product.getStatus());
    }

    private static ProductVariant variant(int stock, boolean dropship, boolean supplierAvailable) {
        ProductVariant v = new ProductVariant();
        v.setStock(stock);
        v.setDropship(dropship);
        v.setSupplierAvailable(supplierAvailable);
        return v;
    }
}
