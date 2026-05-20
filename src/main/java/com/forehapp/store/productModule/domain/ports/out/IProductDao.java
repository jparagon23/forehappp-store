package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IProductDao {
    Optional<Product> findById(Long id);
    Optional<Product> findByIdAndStoreId(Long productId, Long storeId);
    List<Product> findAllByStoreId(Long storeId);
    Page<Product> findActiveProducts(String search, Long categoryId, Long brandId, Pageable pageable);
    Product save(Product product);
    void delete(Product product);
}
