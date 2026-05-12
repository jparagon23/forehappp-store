package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateProductRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Brand is required")
    private Long brandId;

    private Long lineId;

    @NotNull(message = "Category is required")
    private Long categoryId;
}
