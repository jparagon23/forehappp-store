package com.forehapp.store.authModule.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponseDto {
    private Long userId;
    private String message;
}
