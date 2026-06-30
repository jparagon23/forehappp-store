package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class UpdateVariantDto {

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Compare-at price must be greater than 0")
    private BigDecimal compareAtPrice;

    private boolean clearCompareAtPrice = false;

    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be greater than 0")
    private BigDecimal cost;

    @Size(max = 255, message = "Cost notes must not exceed 255 characters")
    private String costNotes;

    private boolean clearCost = false;
}
