package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateLineRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Category is required")
    private Long categoryId;
}
