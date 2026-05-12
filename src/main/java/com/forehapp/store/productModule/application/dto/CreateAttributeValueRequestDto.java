package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateAttributeValueRequestDto {

    @NotBlank(message = "Description is required")
    private String description;
}
