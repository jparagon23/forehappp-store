package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.UserResponse;

public interface GetUserUseCase {
    UserResponse getProfile(Long userId);
}
