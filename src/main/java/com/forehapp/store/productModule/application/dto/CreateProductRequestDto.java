package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateProductRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Brand is required")
    private Long brandId;

    private Long lineId;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Boolean freeShipping = false;

    @Min(value = 1, message = "Repurchase days must be at least 1")
    @Max(value = 365, message = "Repurchase days cannot exceed 365")
    private Integer repurchaseDays;
}
