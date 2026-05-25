package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface IProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByBrandId(Long brandId);
    boolean existsByCategoryId(Long categoryId);
    boolean existsByLineId(Long lineId);

    @EntityGraph(attributePaths = {"brand", "category", "line", "store"})
    List<Product> findAllByStoreId(Long storeId);

    @Override
    @EntityGraph(attributePaths = {"brand", "category", "line"})
    @NonNull
    Page<Product> findAll(Specification<Product> spec, @NonNull Pageable pageable);
}
