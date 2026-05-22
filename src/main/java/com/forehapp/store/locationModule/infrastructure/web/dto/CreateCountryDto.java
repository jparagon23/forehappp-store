package com.forehapp.store.locationModule.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCountryDto(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(min = 2, max = 3) String code
) {}
