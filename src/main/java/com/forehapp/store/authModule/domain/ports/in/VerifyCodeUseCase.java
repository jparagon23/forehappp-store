package com.forehapp.store.authModule.domain.ports.in;

import com.forehapp.store.authModule.application.dto.LoginResponseDto;
import com.forehapp.store.authModule.application.dto.VerifyCodeRequestDto;

public interface VerifyCodeUseCase {
    LoginResponseDto verifyCode(VerifyCodeRequestDto dto);
}
