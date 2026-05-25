package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public Page<Product> findActiveProducts(String search, Long categoryId, Long brandId, Pageable pageable) {
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

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public void delete(Product product) {
        jpaRepository.delete(product);
    }
}
