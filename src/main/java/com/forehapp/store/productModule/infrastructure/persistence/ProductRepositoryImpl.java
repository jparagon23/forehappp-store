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
    public Optional<Product> findByIdAndSellerId(Long productId, Long sellerId) {
        return jpaRepository.findByIdAndSellerId(productId, sellerId);
    }

    @Override
    public List<Product> findAllBySellerId(Long sellerId) {
        return jpaRepository.findAllBySellerId(sellerId);
    }

    @Override
    public Page<Product> findActiveProducts(String search, Long categoryId, Long brandId, Pageable pageable) {
        Specification<Product> spec = Specification.where(ProductSpecification.isActive());
        if (search != null && !search.isBlank()) {
            spec = spec.and(ProductSpecification.titleContains(search));
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
