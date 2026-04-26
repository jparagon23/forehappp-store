package com.forehapp.store.authModule.domain.ports.in;

public interface ResendCodeUseCase {
    void resendCode(Long userId);
}
