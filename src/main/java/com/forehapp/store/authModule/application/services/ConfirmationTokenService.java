package com.forehapp.store.authModule.application.services;

import com.forehapp.store.authModule.domain.model.ConfirmationToken;
import com.forehapp.store.authModule.domain.ports.out.ConfirmationTokenRepository;
import com.forehapp.store.userModule.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository tokenRepository;

    public ConfirmationTokenService(ConfirmationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public ConfirmationToken createToken(User user) {
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        ConfirmationToken token = new ConfirmationToken(
                code,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user
        );
        return tokenRepository.save(token);
    }

    public Optional<ConfirmationToken> findByCode(String code) {
        return tokenRepository.findByToken(code);
    }

    @Transactional
    public void markAsConfirmed(String code) {
        tokenRepository.setConfirmedAt(code, LocalDateTime.now());
    }
}
