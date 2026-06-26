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
    private final IProductRepository productRepository;

    public CategoryRepositoryAdapter(ICategoryRepository categoryRepository,
                                     ICategoryAttributeJpaRepository categoryAttributeRepository,
                                     IProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryAttributeRepository = categoryAttributeRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAllOrdered();
    }

    @Override
    public List<Category> findAllWithActiveProducts() {
        return categoryRepository.findAllWithActiveProducts();
    }

    @Override
    public List<CategoryAttribute> findCategoryAttributes(Long categoryId) {
        return categoryAttributeRepository.findByCategoryIdWithAttribute(categoryId);
    }

    @Override
    public Optional<CategoryAttribute> findCategoryAttribute(Long categoryId, Long attributeId) {
        return categoryAttributeRepository.findByCategoryIdAndAttributeId(categoryId, attributeId);
    }

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Category category) {
        categoryRepository.delete(category);
    }

    @Override
    public boolean isUsedByProducts(Long categoryId) {
        return productRepository.existsByCategoryId(categoryId);
    }

    @Override
    public boolean existsCategoryAttribute(Long categoryId, Long attributeId) {
        return categoryAttributeRepository.existsByCategoryIdAndAttributeId(categoryId, attributeId);
    }

    @Override
    public CategoryAttribute saveCategoryAttribute(CategoryAttribute categoryAttribute) {
        return categoryAttributeRepository.save(categoryAttribute);
    }

    @Override
    public void deleteCategoryAttribute(CategoryAttribute categoryAttribute) {
        categoryAttributeRepository.delete(categoryAttribute);
    }

    @Override
    public boolean existsByDescriptionIgnoreCase(String description) {
        return categoryRepository.existsByDescriptionIgnoreCase(description);
    }
}
