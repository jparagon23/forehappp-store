package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.UpdatePhoneRequestDto;
import com.forehapp.store.userModule.application.dto.UserResponse;

public interface UpdatePhoneUseCase {
    UserResponse updatePhone(Long userId, UpdatePhoneRequestDto dto);
}
