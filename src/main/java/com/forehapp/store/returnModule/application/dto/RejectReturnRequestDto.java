package com.forehapp.store.returnModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectReturnRequestDto(
        @NotBlank @Size(max = 1000) String adminNotes
) {}
