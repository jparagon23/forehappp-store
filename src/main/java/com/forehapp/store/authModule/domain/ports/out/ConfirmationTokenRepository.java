package com.forehapp.store.authModule.domain.ports.out;

import com.forehapp.store.authModule.domain.model.ConfirmationToken;

import java.util.List;
import java.util.Optional;

public interface ConfirmationTokenRepository {
    ConfirmationToken save(ConfirmationToken token);
    /** The user's newest code that has not been used yet. */
    Optional<ConfirmationToken> findLatestPending(Long userId);
    List<ConfirmationToken> findPending(Long userId);
}
