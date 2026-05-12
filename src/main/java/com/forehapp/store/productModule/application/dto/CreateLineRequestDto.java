package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateLineRequestDto {

    @NotBlank(message = "Name is required")
    private String name;
}
