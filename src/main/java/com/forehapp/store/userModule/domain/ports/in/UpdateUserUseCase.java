package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.UpdateUserRequestDto;
import com.forehapp.store.userModule.application.dto.UserResponse;

public interface UpdateUserUseCase {
    UserResponse updateProfile(Long userId, UpdateUserRequestDto dto);
}
