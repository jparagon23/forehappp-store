package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.storeModule.domain.model.Store;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PublicProductDetailResponse {

    public record SellerInfo(Long id, String name, String slug, String logoUrl) {
        public static SellerInfo from(Store store, String logoUrl) {
            return new SellerInfo(store.getId(), store.getName(), store.getSlug(), logoUrl);
        }
    }

    private final Long id;
    private final String title;
    private final String description;
    private final String brand;
    private final String line;
    private final String category;
    private final LocalDateTime createdAt;
    private final boolean freeShipping;
    private final List<ProductVariantResponse> variants;
    private final List<ProductImageResponse> images;
    private final SellerInfo store;

    public PublicProductDetailResponse(Product product, List<ProductImageResponse> images, SellerInfo store) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.brand = product.getBrand().getDescription();
        this.line = product.getLine() != null ? product.getLine().getDescription() : null;
        this.category = product.getCategory().getDescription();
        this.createdAt = product.getCreatedAt();
        this.freeShipping = Boolean.TRUE.equals(product.getFreeShipping());
        this.variants = product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .map(ProductVariantResponse::new)
                .toList();
        this.images = images;
        this.store = store;
    }
}
