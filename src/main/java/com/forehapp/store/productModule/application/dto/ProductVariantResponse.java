package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.ProductVariant;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
public class ProductVariantResponse {

    private final Long id;
    private final String sku;
    private final BigDecimal price;
    private final BigDecimal compareAtPrice;
    private final BigDecimal cost;
    private final BigDecimal margin;
    private final BigDecimal marginPercent;
    private final Integer stock;
    private final Boolean active;
    private final List<AttributeValueInfo> attributes;

    public ProductVariantResponse(ProductVariant variant) {
        this.id = variant.getId();
        this.sku = variant.getSku();
        this.price = variant.getPrice();
        this.compareAtPrice = variant.getCompareAtPrice();
        this.cost = variant.getCost();
        this.stock = variant.getStock();
        this.active = variant.getActive();
        this.attributes = variant.getAttributeValues().stream()
                .map(av -> new AttributeValueInfo(
                        av.getId(),
                        av.getAttribute().getDescription(),
                        av.getDescription()))
                .toList();

        if (variant.getCost() != null) {
            this.margin = variant.getPrice().subtract(variant.getCost());
            this.marginPercent = variant.getPrice().compareTo(BigDecimal.ZERO) > 0
                    ? this.margin.divide(variant.getPrice(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                    : null;
        } else {
            this.margin = null;
            this.marginPercent = null;
        }
    }

    public record AttributeValueInfo(Long id, String attribute, String value) {}
}
