package com.forehapp.store.authModule.application.services;

import com.forehapp.store.authModule.domain.model.ConfirmationToken;
import com.forehapp.store.authModule.domain.ports.out.ConfirmationTokenRepository;
import com.forehapp.store.userModule.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 6-digit email codes. Only the user's newest code works (a new one cancels the old ones), it lasts
 * 15 minutes and stops working after {@value #MAX_ATTEMPTS} wrong tries, so it cannot be guessed.
 */
@Service
public class ConfirmationTokenService {

    public enum Result { VALID, INVALID, EXPIRED, LOCKED }

    static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ConfirmationTokenRepository tokenRepository;

    public ConfirmationTokenService(ConfirmationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public ConfirmationToken createToken(User user) {
        LocalDateTime now = LocalDateTime.now();
        for (ConfirmationToken old : tokenRepository.findPending(user.getId())) {
            if (old.getExpiresAt().isAfter(now)) {
                old.setExpiresAt(now);
                tokenRepository.save(old);
            }
        }
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        return tokenRepository.save(new ConfirmationToken(code, now, now.plusMinutes(15), user));
    }

    /**
     * Checks the code and marks it used when it matches. Runs in its own transaction so a wrong
     * attempt is counted even though the caller then fails the request.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result verify(Long userId, String code) {
        ConfirmationToken token = tokenRepository.findLatestPending(userId).orElse(null);
        if (token == null || code == null) return Result.INVALID;

        LocalDateTime now = LocalDateTime.now();
        if (token.getAttempts() >= MAX_ATTEMPTS) return Result.LOCKED;
        if (token.getExpiresAt().isBefore(now)) return Result.EXPIRED;

        boolean matches = MessageDigest.isEqual(token.getToken().getBytes(StandardCharsets.UTF_8),
                code.trim().getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            token.setAttempts(token.getAttempts() + 1);
            tokenRepository.save(token);
            return token.getAttempts() >= MAX_ATTEMPTS ? Result.LOCKED : Result.INVALID;
        }
        token.setConfirmedAt(now);
        tokenRepository.save(token);
        return Result.VALID;
    }
}
