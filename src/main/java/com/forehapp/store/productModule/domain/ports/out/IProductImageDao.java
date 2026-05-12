package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.ProductImage;

import java.util.List;
import java.util.Optional;

public interface IProductImageDao {
    ProductImage save(ProductImage image);
    Optional<ProductImage> findById(Long id);
    List<ProductImage> findByProductId(Long productId);
    boolean existsByProductId(Long productId);
    void delete(ProductImage image);
    void deleteAllByProductId(Long productId);
}
