package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.ProductVariant;

import java.util.Optional;

public interface IProductVariantDao {
    Optional<ProductVariant> findById(Long variantId);
    Optional<ProductVariant> findByIdAndProductId(Long variantId, Long productId);
    boolean existsBySku(String sku);
    ProductVariant save(ProductVariant variant);
    void delete(ProductVariant variant);
}
