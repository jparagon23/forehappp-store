package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.BrandCount;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductDiscoverySection;
import com.forehapp.store.productModule.domain.model.ProductSortBy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IProductDao {
    Optional<Product> findById(Long id);
    Optional<Product> findByIdAndStoreId(Long productId, Long storeId);
    List<Product> findAllByStoreId(Long storeId);
    Page<Product> findActiveProducts(String search, Long categoryId, Long brandId, Boolean freeShipping, ProductSortBy sortBy, Pageable pageable);
    List<BrandCount> findBrandFacets(String search, Long categoryId, Boolean freeShipping);
    List<ProductDiscoverySection> findDiscoverySections(int limit);
    Product save(Product product);
    void delete(Product product);
}
