package com.forehapp.store.userModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateUserRequestDto {

    @NotBlank(message = "El nombre es requerido")
    private String name;

    @NotBlank(message = "El apellido es requerido")
    private String lastname;
}
