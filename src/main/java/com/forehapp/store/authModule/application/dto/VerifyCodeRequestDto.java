package com.forehapp.store.authModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VerifyCodeRequestDto {

    @NotNull(message = "El userId es requerido")
    private Long userId;

    @NotBlank(message = "El código es requerido")
    private String code;
}
