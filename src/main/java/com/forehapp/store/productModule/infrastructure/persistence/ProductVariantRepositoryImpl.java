package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProductVariantRepositoryImpl implements IProductVariantDao {

    private final IProductVariantRepository jpaRepository;

    public ProductVariantRepositoryImpl(IProductVariantRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ProductVariant> findById(Long variantId) {
        return jpaRepository.findById(variantId);
    }

    @Override
    public Optional<ProductVariant> findByIdAndProductId(Long variantId, Long productId) {
        return jpaRepository.findByIdAndProductId(variantId, productId);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public ProductVariant save(ProductVariant variant) {
        return jpaRepository.save(variant);
    }

    @Override
    public void delete(ProductVariant variant) {
        jpaRepository.delete(variant);
    }
}
