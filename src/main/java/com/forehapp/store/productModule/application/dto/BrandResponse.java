package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.Brand;

public record BrandResponse(Long id, String name) {
    public BrandResponse(Brand brand) {
        this(brand.getId(), brand.getDescription());
    }
}
