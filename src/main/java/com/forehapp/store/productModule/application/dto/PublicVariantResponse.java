package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.ProductVariant;

import java.math.BigDecimal;
import java.util.List;

/**
 * Variant as shown to buyers. Never add cost, margin or other seller-only data here: the buyer must not
 * tell own stock from dropshipping either.
 * maxQuantity: most units that can be ordered now; null = no limit.
 */
public record PublicVariantResponse(
        Long id,
        String sku,
        BigDecimal price,
        BigDecimal compareAtPrice,
        boolean available,
        Integer maxQuantity,
        Boolean active,
        List<ProductVariantResponse.AttributeValueInfo> attributes
) {
    public static PublicVariantResponse from(ProductVariant variant) {
        return new PublicVariantResponse(
                variant.getId(),
                variant.getSku(),
                variant.getPrice(),
                variant.getCompareAtPrice(),
                variant.isSellable(),
                variant.isSellable() ? variant.maxQuantity() : Integer.valueOf(0),
                variant.getActive(),
                variant.getAttributeValues().stream()
                        .map(av -> new ProductVariantResponse.AttributeValueInfo(
                                av.getId(),
                                av.getAttribute().getDescription(),
                                av.getDescription()))
                        .toList());
    }
}
