package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.ChangePasswordDto;

public interface ChangePasswordUseCase {
    void changePassword(Long userId, ChangePasswordDto dto);
}
