package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductDiscoverySection;
import com.forehapp.store.productModule.domain.model.ProductSortBy;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductRepositoryImpl implements IProductDao {

    private final IProductRepository jpaRepository;

    public ProductRepositoryImpl(IProductRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Product> findByIdAndStoreId(Long productId, Long storeId) {
        return jpaRepository.findByIdAndStoreId(productId, storeId);
    }

    @Override
    public List<Product> findAllByStoreId(Long storeId) {
        return jpaRepository.findAllByStoreId(storeId);
    }

    @Override
    public Page<Product> findActiveProducts(String search, Long categoryId, Long brandId, ProductSortBy sortBy, Pageable pageable) {
        if (sortBy == ProductSortBy.DISCOVERY && search == null && categoryId == null && brandId == null) {
            return findDiscoveryProducts(pageable);
        }

        Specification<Product> spec = Specification.where(ProductSpecification.isActive());
        if (search != null && !search.isBlank()) {
            spec = spec.and(ProductSpecification.matchesSearch(search));
        }
        if (categoryId != null) {
            spec = spec.and(ProductSpecification.hasCategory(categoryId));
        }
        if (brandId != null) {
            spec = spec.and(ProductSpecification.hasBrand(brandId));
        }
        return jpaRepository.findAll(spec, pageable);
    }

    private Page<Product> findDiscoveryProducts(Pageable pageable) {
        int seed = LocalDate.now().getDayOfYear();
        Pageable idPage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        List<Long> ids = jpaRepository.findDiscoveryProductIds(seed, idPage);
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        long total = jpaRepository.countActiveStoreProducts();

        Map<Long, Integer> order = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) order.put(ids.get(i), i);

        List<Product> products = jpaRepository.findByIdInWithGraph(ids)
                .stream()
                .sorted(Comparator.comparingInt(p -> order.get(p.getId())))
                .toList();

        return new PageImpl<>(products, pageable, total);
    }

    @Override
    public List<ProductDiscoverySection> findDiscoverySections(int limit) {
        int seed = LocalDate.now().getDayOfYear();
        List<DiscoveryProductIdView> entries = jpaRepository.findDiscoverySectionIds(seed, limit);
        if (entries.isEmpty()) return List.of();

        List<Long> ids = entries.stream().map(DiscoveryProductIdView::getProductId).toList();
        Map<Long, Product> productMap = jpaRepository.findByIdInWithGraph(ids)
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        Map<Long, List<Product>> byCategory = new LinkedHashMap<>();
        Map<Long, Long> categoryTotals = new LinkedHashMap<>();
        for (DiscoveryProductIdView entry : entries) {
            Product product = productMap.get(entry.getProductId());
            if (product == null) continue;
            byCategory.computeIfAbsent(entry.getCategoryId(), k -> new ArrayList<>()).add(product);
            categoryTotals.put(entry.getCategoryId(), entry.getCatTotal());
        }

        return byCategory.entrySet().stream()
                .map(e -> new ProductDiscoverySection(e.getKey(), categoryTotals.get(e.getKey()), e.getValue()))
                .toList();
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public void delete(Product product) {
        jpaRepository.delete(product);
    }
}
