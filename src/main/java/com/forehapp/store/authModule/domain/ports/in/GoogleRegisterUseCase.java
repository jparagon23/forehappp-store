package com.forehapp.store.authModule.domain.ports.in;

import com.forehapp.store.authModule.application.dto.LoginResponseDto;

public interface GoogleRegisterUseCase {
    LoginResponseDto registerWithGoogle(String idToken);
}
