package com.forehapp.store.locationModule.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStateDto(
        @NotBlank @Size(max = 150) String name,
        @NotNull Long countryId
) {}
