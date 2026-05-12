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
    private final int variantCount;

    public PublicProductSummaryResponse(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.brand = product.getBrand().getDescription();
        this.line = product.getLine() != null ? product.getLine().getDescription() : null;
        this.category = product.getCategory().getDescription();
        this.createdAt = product.getCreatedAt();
        this.variantCount = product.getVariants().size();
        this.minPrice = product.getVariants().stream()
                .map(v -> v.getPrice())
                .min(BigDecimal::compareTo)
                .orElse(null);
    }
}
