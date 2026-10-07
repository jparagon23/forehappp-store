package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.ProductVariant;

import java.math.BigDecimal;
import java.util.List;

/**
 * Variant as shown to buyers. Never add cost, margin or other seller-only data here.
 */
public record PublicVariantResponse(
        Long id,
        String sku,
        BigDecimal price,
        BigDecimal compareAtPrice,
        Integer stock,
        Boolean active,
        List<ProductVariantResponse.AttributeValueInfo> attributes
) {
    public static PublicVariantResponse from(ProductVariant variant) {
        return new PublicVariantResponse(
                variant.getId(),
                variant.getSku(),
                variant.getPrice(),
                variant.getCompareAtPrice(),
                variant.getStock(),
                variant.getActive(),
                variant.getAttributeValues().stream()
                        .map(av -> new ProductVariantResponse.AttributeValueInfo(
                                av.getId(),
                                av.getAttribute().getDescription(),
                                av.getDescription()))
                        .toList());
    }
}
