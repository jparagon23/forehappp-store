package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.Product;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductResponse {

    private final Long id;
    private final String description;
    private final String brand;
    private final String line;
    private final String category;
    private final String status;
    private final List<ProductVariantResponse> variants;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.description = product.getDescription();
        this.brand = product.getBrand().getDescription();
        this.line = product.getLine() != null ? product.getLine().getDescription() : null;
        this.category = product.getCategory().getDescription();
        this.status = product.getStatus().name();
        this.variants = product.getVariants().stream()
                .map(ProductVariantResponse::new)
                .toList();
    }
}
