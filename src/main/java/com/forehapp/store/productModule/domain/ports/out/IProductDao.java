package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.Product;

import java.util.Optional;

public interface IProductDao {
    Optional<Product> findById(Long id);
    Optional<Product> findByIdAndSellerId(Long productId, Long sellerId);
    Product save(Product product);
}
