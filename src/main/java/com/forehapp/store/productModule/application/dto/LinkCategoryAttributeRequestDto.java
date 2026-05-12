package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LinkCategoryAttributeRequestDto {

    @NotNull(message = "Attribute ID is required")
    private Long attributeId;

    private boolean required;
}
