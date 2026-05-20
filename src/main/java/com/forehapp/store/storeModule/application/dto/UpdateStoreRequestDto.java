package com.forehapp.store.storeModule.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateStoreRequestDto {

    @Size(min = 2, max = 150)
    private String name;

    @Size(max = 1000)
    private String description;
}
