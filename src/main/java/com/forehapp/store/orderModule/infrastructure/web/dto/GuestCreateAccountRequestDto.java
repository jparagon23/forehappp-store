package com.forehapp.store.orderModule.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestCreateAccountRequestDto(
        @NotBlank @Email(message = "Valid email is required") String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password
) {}
