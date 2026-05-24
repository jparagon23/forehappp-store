package com.forehapp.store.storeModule.application.dto;

import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InviteMemberRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    private StoreMemberRole role;
}
