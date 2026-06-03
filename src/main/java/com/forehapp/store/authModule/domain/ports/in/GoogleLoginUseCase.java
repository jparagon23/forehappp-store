package com.forehapp.store.authModule.domain.ports.in;

import com.forehapp.store.authModule.application.dto.LoginResponseDto;

public interface GoogleLoginUseCase {
    LoginResponseDto loginWithGoogle(String idToken);
}
