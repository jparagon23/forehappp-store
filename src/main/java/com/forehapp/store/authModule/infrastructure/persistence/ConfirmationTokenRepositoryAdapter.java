package com.forehapp.store.authModule.infrastructure.persistence;

import com.forehapp.store.authModule.domain.model.ConfirmationToken;
import com.forehapp.store.authModule.domain.ports.out.ConfirmationTokenRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class ConfirmationTokenRepositoryAdapter implements ConfirmationTokenRepository {

    private final ConfirmationTokenJpaRepository jpaRepository;

    public ConfirmationTokenRepositoryAdapter(ConfirmationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ConfirmationToken save(ConfirmationToken token) {
        return jpaRepository.save(token);
    }

    @Override
    public Optional<ConfirmationToken> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    public void setConfirmedAt(String token, LocalDateTime now) {
        jpaRepository.setConfirmedAt(token, now);
    }
}
