package com.forehapp.store.userModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdatePhoneRequestDto {

    @NotBlank(message = "Phone number is required")
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    @Pattern(regexp = "^[+\\d][\\d\\s\\-().]{4,49}$", message = "Invalid phone number format")
    private String phone;
}
