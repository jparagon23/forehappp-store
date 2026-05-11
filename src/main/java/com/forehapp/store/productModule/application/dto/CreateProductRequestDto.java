package com.forehapp.store.productModule.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CreateProductRequestDto {

    @NotBlank(message = "La descripción es requerida")
    private String description;

    @NotNull(message = "La marca es requerida")
    private Long brandId;

    private Long lineId;

    @NotNull(message = "La categoría es requerida")
    private Long categoryId;

    @NotEmpty(message = "El producto debe tener al menos una variante")
    @Valid
    private List<CreateVariantDto> variants;
}
