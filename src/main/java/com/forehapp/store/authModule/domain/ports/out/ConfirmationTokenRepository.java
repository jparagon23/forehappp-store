package com.forehapp.store.authModule.domain.ports.out;

import com.forehapp.store.authModule.domain.model.ConfirmationToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConfirmationTokenRepository {
    ConfirmationToken save(ConfirmationToken token);
    Optional<ConfirmationToken> findByToken(String token);
    void setConfirmedAt(String token, LocalDateTime now);
}
