package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ICategoryAttributeJpaRepository extends JpaRepository<CategoryAttribute, Long> {

    @Query("SELECT ca FROM CategoryAttribute ca JOIN FETCH ca.attribute WHERE ca.category.id = :categoryId")
    List<CategoryAttribute> findByCategoryIdWithAttribute(@Param("categoryId") Long categoryId);

    boolean existsByCategoryIdAndAttributeId(Long categoryId, Long attributeId);
}
