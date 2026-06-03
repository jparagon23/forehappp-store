package com.forehapp.store.authModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class GoogleLoginRequestDto {

    @NotBlank(message = "El token de Google es obligatorio")
    private String idToken;
}
