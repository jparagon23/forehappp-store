package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.BrandCount;

public record BrandFacetResponse(Long id, String name, long count) {
    public static BrandFacetResponse from(BrandCount bc) {
        return new BrandFacetResponse(bc.brandId(), bc.brandName(), bc.count());
    }
}
