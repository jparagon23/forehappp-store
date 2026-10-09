package com.forehapp.store.authModule.infrastructure.persistence;

import com.forehapp.store.authModule.domain.model.AuthSession;
import com.forehapp.store.authModule.domain.ports.out.AuthSessionRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AuthSessionRepositoryAdapter implements AuthSessionRepository {

    private final AuthSessionJpaRepository jpaRepository;

    public AuthSessionRepositoryAdapter(AuthSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuthSession save(AuthSession session) {
        return jpaRepository.save(session);
    }

    @Override
    public Optional<AuthSession> findById(String sessionId) {
        return jpaRepository.findById(sessionId);
    }
}
