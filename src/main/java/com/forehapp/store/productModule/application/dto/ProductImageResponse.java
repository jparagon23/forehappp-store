package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.ProductImage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductImageResponse {
    private final Long id;
    private final Long productId;
    private final String url;
    private final Integer displayOrder;
    private final LocalDateTime createdAt;

    public ProductImageResponse(ProductImage image) {
        this.id = image.getId();
        this.productId = image.getProduct().getId();
        this.url = image.getUrl();
        this.displayOrder = image.getDisplayOrder();
        this.createdAt = image.getCreatedAt();
    }
}
