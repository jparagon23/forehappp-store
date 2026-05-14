package com.forehapp.store.returnModule.application.dto;

import com.forehapp.store.returnModule.domain.model.ReturnType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateReturnRequestDto(
        @NotNull Long orderGroupId,
        @NotNull ReturnType returnType,
        @NotBlank @Size(max = 1000) String reason,
        @NotEmpty @Valid List<ReturnItemRequestDto> items
) {}
