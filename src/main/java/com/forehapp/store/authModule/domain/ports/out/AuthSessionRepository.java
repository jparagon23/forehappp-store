package com.forehapp.store.authModule.domain.ports.out;

import com.forehapp.store.authModule.domain.model.AuthSession;

import java.util.Optional;

public interface AuthSessionRepository {
    AuthSession save(AuthSession session);
    Optional<AuthSession> findById(String sessionId);
}
