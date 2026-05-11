package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.ProductVariant;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ProductVariantResponse {

    private final Long id;
    private final String sku;
    private final BigDecimal price;
    private final Integer stock;
    private final List<AttributeValueInfo> attributes;

    public ProductVariantResponse(ProductVariant variant) {
        this.id = variant.getId();
        this.sku = variant.getSku();
        this.price = variant.getPrice();
        this.stock = variant.getStock();
        this.attributes = variant.getAttributeValues().stream()
                .map(av -> new AttributeValueInfo(
                        av.getId(),
                        av.getAttribute().getDescription(),
                        av.getDescription()))
                .toList();
    }

    public record AttributeValueInfo(Long id, String attribute, String value) {}
}
