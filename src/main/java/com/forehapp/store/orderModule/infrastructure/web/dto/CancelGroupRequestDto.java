package com.forehapp.store.orderModule.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelGroupRequestDto(
        @NotBlank @Size(max = 500) String reason
) {}
