package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ICategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c ORDER BY c.sortOrder ASC, c.id ASC")
    List<Category> findAllOrdered();

    @Query("SELECT c FROM Category c WHERE EXISTS (SELECT p FROM Product p WHERE p.category = c AND p.status = com.forehapp.store.productModule.domain.model.ProductStatus.ACTIVE) ORDER BY c.sortOrder ASC, c.id ASC")
    List<Category> findAllWithActiveProducts();
}
