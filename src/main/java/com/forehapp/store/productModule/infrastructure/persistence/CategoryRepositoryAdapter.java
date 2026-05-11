package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Category;
import com.forehapp.store.productModule.domain.model.CategoryAttribute;
import com.forehapp.store.productModule.domain.ports.out.ICategoryDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryAdapter implements ICategoryDao {

    private final ICategoryRepository categoryRepository;
    private final ICategoryAttributeJpaRepository categoryAttributeRepository;

    public CategoryRepositoryAdapter(ICategoryRepository categoryRepository,
                                     ICategoryAttributeJpaRepository categoryAttributeRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryAttributeRepository = categoryAttributeRepository;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<CategoryAttribute> findCategoryAttributes(Long categoryId) {
        return categoryAttributeRepository.findByCategoryIdWithAttribute(categoryId);
    }
}
