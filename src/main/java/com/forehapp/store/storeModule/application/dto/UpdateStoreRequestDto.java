package com.forehapp.store.storeModule.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UpdateStoreRequestDto {

    @Size(min = 2, max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    @DecimalMin("0")
    private BigDecimal freeShippingMinAmount;
}
