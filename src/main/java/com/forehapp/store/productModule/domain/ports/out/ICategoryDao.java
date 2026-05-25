package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.Category;
import com.forehapp.store.productModule.domain.model.CategoryAttribute;

import java.util.List;
import java.util.Optional;

public interface ICategoryDao {
    Optional<Category> findById(Long id);
    List<Category> findAll();
    List<CategoryAttribute> findCategoryAttributes(Long categoryId);
    Optional<CategoryAttribute> findCategoryAttribute(Long categoryId, Long attributeId);
    Category save(Category category);
    void delete(Category category);
    boolean isUsedByProducts(Long categoryId);
    boolean existsCategoryAttribute(Long categoryId, Long attributeId);
    CategoryAttribute saveCategoryAttribute(CategoryAttribute categoryAttribute);
    void deleteCategoryAttribute(CategoryAttribute categoryAttribute);
}
