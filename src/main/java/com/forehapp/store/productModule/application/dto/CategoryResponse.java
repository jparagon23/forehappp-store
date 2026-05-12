package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.Category;

public record CategoryResponse(Long id, String name) {
    public CategoryResponse(Category category) {
        this(category.getId(), category.getDescription());
    }
}
