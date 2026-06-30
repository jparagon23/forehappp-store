package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class CreateVariantDto {

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Compare-at price must be greater than 0")
    private BigDecimal compareAtPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than 0")
    private BigDecimal cost;

    @Size(max = 255, message = "Cost notes must not exceed 255 characters")
    private String costNotes;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private List<Long> attributeValueIds = new ArrayList<>();
}
