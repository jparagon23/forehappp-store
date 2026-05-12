package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IProductImageJpaRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct_Id(Long productId);
    boolean existsByProduct_Id(Long productId);
}
