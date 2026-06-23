package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateCategoryDiscoveryOrderRequestDto {

    @NotNull(message = "sortOrder is required")
    @Min(value = 1, message = "sortOrder must be at least 1")
    private Integer sortOrder;
}
