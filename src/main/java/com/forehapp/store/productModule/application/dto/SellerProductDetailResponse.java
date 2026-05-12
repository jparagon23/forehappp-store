package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.Product;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SellerProductDetailResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String brand;
    private final String line;
    private final String category;
    private final String status;
    private final LocalDateTime createdAt;
    private final List<ProductVariantResponse> variants;
    private final List<ProductImageResponse> images;

    public SellerProductDetailResponse(Product product, List<ProductImageResponse> images) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.brand = product.getBrand().getDescription();
        this.line = product.getLine() != null ? product.getLine().getDescription() : null;
        this.category = product.getCategory().getDescription();
        this.status = product.getStatus().name();
        this.createdAt = product.getCreatedAt();
        this.variants = product.getVariants().stream()
                .map(ProductVariantResponse::new)
                .toList();
        this.images = images;
    }
}
