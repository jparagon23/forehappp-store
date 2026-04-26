package com.forehapp.store.authModule.domain.ports.in;

import com.forehapp.store.authModule.application.dto.RegisterRequestDto;
import com.forehapp.store.authModule.application.dto.RegisterResponseDto;

public interface RegisterUseCase {
    RegisterResponseDto register(RegisterRequestDto dto);
}
