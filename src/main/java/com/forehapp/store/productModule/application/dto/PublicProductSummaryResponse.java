package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PublicProductSummaryResponse {

    private final Long id;
    private final String title;
    private final String brand;
    private final String line;
    private final String category;
    private final LocalDateTime createdAt;
    private final BigDecimal minPrice;
    private final BigDecimal compareAtPrice;
    private final int variantCount;
    private final boolean freeShipping;
    private final String thumbnailUrl;

    public PublicProductSummaryResponse(Product product, String thumbnailUrl) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.brand = product.getBrand().getDescription();
        this.line = product.getLine() != null ? product.getLine().getDescription() : null;
        this.category = product.getCategory().getDescription();
        this.createdAt = product.getCreatedAt();
        var activeVariants = product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .toList();
        this.variantCount = activeVariants.size();
        this.minPrice = activeVariants.stream()
                .map(v -> v.getPrice())
                .min(BigDecimal::compareTo)
                .orElse(null);
        this.compareAtPrice = activeVariants.stream()
                .map(v -> v.getCompareAtPrice())
                .filter(p -> p != null)
                .max(BigDecimal::compareTo)
                .orElse(null);
        this.freeShipping = Boolean.TRUE.equals(product.getFreeShipping());
        this.thumbnailUrl = thumbnailUrl;
    }
}
