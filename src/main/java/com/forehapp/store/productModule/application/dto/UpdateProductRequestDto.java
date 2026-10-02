package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProductRequestDto {

    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    private String description;

    private Long brandId;

    private Long lineId;

    private Boolean freeShipping;

    @Min(value = 1, message = "Repurchase days must be at least 1")
    @Max(value = 365, message = "Repurchase days cannot exceed 365")
    private Integer repurchaseDays;

    private boolean clearRepurchaseDays = false;
}
