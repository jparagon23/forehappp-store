package com.forehapp.store.security;

import com.forehapp.store.authModule.application.services.ConfirmationTokenService;
import com.forehapp.store.authModule.application.services.ConfirmationTokenService.Result;
import com.forehapp.store.authModule.domain.model.ConfirmationToken;
import com.forehapp.store.authModule.domain.ports.out.ConfirmationTokenRepository;
import com.forehapp.store.security.filter.RateLimitFilterAccess;
import com.forehapp.store.userModule.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityHardeningTest {

    // ── Client IP behind Railway (one proxy that appends the real address) ──

    @Test
    void spoofedForwardedForIsIgnored() {
        // The client sent "1.2.3.4"; Railway appended the real address
        assertEquals("190.0.0.9", RateLimitFilterAccess.clientIp("1.2.3.4, 190.0.0.9", "10.0.0.1", 1));
        assertEquals("190.0.0.9", RateLimitFilterAccess.clientIp("190.0.0.9", "10.0.0.1", 1));
        assertEquals("10.0.0.1", RateLimitFilterAccess.clientIp(null, "10.0.0.1", 1));
        assertEquals("10.0.0.1", RateLimitFilterAccess.clientIp("1.2.3.4", "10.0.0.1", 0));
    }

    // ── Verification codes ──

    @Test
    void codeIsLockedAfterFiveWrongAttempts() {
        FakeRepo repo = new FakeRepo();
        ConfirmationTokenService service = new ConfirmationTokenService(repo);
        User user = user(7L);
        String code = service.createToken(user).getToken();
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 4; i++) assertEquals(Result.INVALID, service.verify(7L, wrong));
        assertEquals(Result.LOCKED, service.verify(7L, wrong));
        // Even the right code no longer works
        assertEquals(Result.LOCKED, service.verify(7L, code));
    }

    @Test
    void onlyTheNewestCodeWorksAndOnlyOnce() {
        FakeRepo repo = new FakeRepo();
        ConfirmationTokenService service = new ConfirmationTokenService(repo);
        User user = user(7L);
        String first = service.createToken(user).getToken();
        String second = service.createToken(user).getToken();

        if (!first.equals(second)) assertEquals(Result.INVALID, service.verify(7L, first));
        assertEquals(Result.VALID, service.verify(7L, second));
        assertEquals(Result.INVALID, service.verify(7L, second));
        // Another user's code is never accepted
        assertEquals(Result.INVALID, service.verify(8L, second));
    }

    @Test
    void codesAreSixDigits() {
        ConfirmationTokenService service = new ConfirmationTokenService(new FakeRepo());
        for (int i = 0; i < 50; i++) {
            assertTrue(service.createToken(user(1L)).getToken().matches("\\d{6}"));
        }
    }

    private static User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private static class FakeRepo implements ConfirmationTokenRepository {
        private final List<ConfirmationToken> tokens = new ArrayList<>();
        private long seq = 1;

        @Override
        public ConfirmationToken save(ConfirmationToken token) {
            if (token.getId() == null) {
                token.setId(seq++);
                tokens.add(token);
            }
            return token;
        }

        @Override
        public Optional<ConfirmationToken> findLatestPending(Long userId) {
            return findPending(userId).stream().max(Comparator.comparing(ConfirmationToken::getId));
        }

        @Override
        public List<ConfirmationToken> findPending(Long userId) {
            return tokens.stream()
                    .filter(t -> t.getUser().getId().equals(userId) && t.getConfirmedAt() == null)
                    .toList();
        }
    }

    @SuppressWarnings("unused")
    private static LocalDateTime now() { return LocalDateTime.now(); }
}
