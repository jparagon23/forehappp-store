package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.Category;
import com.forehapp.store.productModule.domain.model.CategoryAttribute;

import java.util.List;
import java.util.Optional;

public interface ICategoryDao {
    Optional<Category> findById(Long id);
    List<CategoryAttribute> findCategoryAttributes(Long categoryId);
}
