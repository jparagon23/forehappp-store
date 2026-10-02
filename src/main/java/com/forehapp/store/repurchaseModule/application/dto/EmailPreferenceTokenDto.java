package com.forehapp.store.repurchaseModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmailPreferenceTokenDto {

    @NotBlank(message = "Token is required")
    private String token;
}
