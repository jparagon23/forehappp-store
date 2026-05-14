package com.forehapp.store.userModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateAddressDto {

    @Size(max = 100)
    private String alias;

    @NotBlank
    @Size(max = 255)
    private String street;

    @NotBlank
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @NotBlank
    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String zipCode;

    private Boolean isDefault = false;
}
