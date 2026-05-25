package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Brand;
import com.forehapp.store.productModule.domain.ports.out.IBrandDao;
import com.forehapp.store.productModule.infrastructure.persistence.IProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BrandRepositoryAdapter implements IBrandDao {

    private final IBrandRepository jpaRepository;
    private final IProductRepository productRepository;

    public BrandRepositoryAdapter(IBrandRepository jpaRepository,
                                   IProductRepository productRepository) {
        this.jpaRepository = jpaRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Brand> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Brand> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Brand save(Brand brand) {
        return jpaRepository.save(brand);
    }

    @Override
    public void delete(Brand brand) {
        jpaRepository.delete(brand);
    }

    @Override
    public boolean isUsedByProducts(Long brandId) {
        return productRepository.existsByBrandId(brandId);
    }
}
