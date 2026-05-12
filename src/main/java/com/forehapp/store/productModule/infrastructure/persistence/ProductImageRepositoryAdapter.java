package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.ProductImage;
import com.forehapp.store.productModule.domain.ports.out.IProductImageDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductImageRepositoryAdapter implements IProductImageDao {

    private final IProductImageJpaRepository jpaRepository;

    public ProductImageRepositoryAdapter(IProductImageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductImage save(ProductImage image) {
        return jpaRepository.save(image);
    }

    @Override
    public Optional<ProductImage> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ProductImage> findByProductId(Long productId) {
        return jpaRepository.findByProduct_Id(productId);
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return jpaRepository.existsByProduct_Id(productId);
    }

    @Override
    public void delete(ProductImage image) {
        jpaRepository.delete(image);
    }
}
