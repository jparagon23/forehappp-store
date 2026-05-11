package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.Product;

import java.util.Optional;

public interface IProductDao {
    Optional<Product> findById(Long id);
    Product save(Product product);
}
