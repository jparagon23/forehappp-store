package com.forehapp.store.storeModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateStoreRequestDto {

    @NotBlank
    @Size(min = 2, max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    private Long ownerId;
}
