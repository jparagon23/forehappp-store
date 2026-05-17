package com.forehapp.store.authModule.application.dto;

import com.forehapp.store.userModule.domain.model.StoreRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String access_token;
    private String refresh_token;
    private Long userId;
    private String name;
    private String email;
    private Set<StoreRole> storeRoles;
}
