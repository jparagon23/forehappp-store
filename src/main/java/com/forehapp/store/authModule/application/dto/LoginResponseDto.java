package com.forehapp.store.authModule.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String access_token;
    private String refresh_token;
    private Long userId;
    private String name;
    private String email;
}
