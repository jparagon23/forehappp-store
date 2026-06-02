package com.forehapp.store.productModule.application.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CategoryDiscoverySectionResponse {

    private final Long categoryId;
    private final String categoryName;
    private final long totalInCategory;
    private final List<PublicProductSummaryResponse> products;

    public CategoryDiscoverySectionResponse(Long categoryId, String categoryName,
                                            long totalInCategory,
                                            List<PublicProductSummaryResponse> products) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.totalInCategory = totalInCategory;
        this.products = products;
    }
}
