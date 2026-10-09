package com.forehapp.store.authModule.application.services;

import com.forehapp.store.authModule.domain.model.AuthSession;
import com.forehapp.store.authModule.domain.ports.out.AuthSessionRepository;
import com.forehapp.store.general.constants.Constants;
import com.forehapp.store.security.config.UserDetailsImpl;
import com.forehapp.store.security.jwt.JwtUtil;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Login sessions. Every login opens one; its tokens carry the session id. Refresh only works while
 * the session is open and the user is still active, and closing it (logout) also stops its access
 * token within {@link #ACTIVE_CACHE_TTL}.
 */
@Service
public class AuthSessionService {

    private static final Logger log = LoggerFactory.getLogger(AuthSessionService.class);

    /** How long an "is this session open" answer is reused, so requests do not hit the database each time. */
    static final Duration ACTIVE_CACHE_TTL = Duration.ofSeconds(30);

    public record Tokens(String accessToken, String refreshToken) {}

    private final AuthSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final Cache<String, Boolean> activeCache = Caffeine.newBuilder()
            .expireAfterWrite(ACTIVE_CACHE_TTL)
            .maximumSize(100_000)
            .build();

    public AuthSessionService(AuthSessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Tokens open(User user) {
        LocalDateTime now = LocalDateTime.now();
        AuthSession session = new AuthSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(user.getId());
        session.setCreatedAt(now);
        session.setExpiresAt(now.plusSeconds(JwtUtil.refreshValiditySeconds()));
        sessionRepository.save(session);
        return issue(user, session.getId());
    }

    /** New token pair for an open session of an active user; empty otherwise. */
    @Transactional
    public Optional<Tokens> refresh(String refreshToken) {
        Claims claims = JwtUtil.parseRefreshToken(refreshToken);
        if (claims == null) return Optional.empty();

        String sessionId = claims.get(JwtUtil.SESSION_CLAIM, String.class);
        if (sessionId == null) return Optional.empty(); // issued before sessions existed: log in again

        LocalDateTime now = LocalDateTime.now();
        AuthSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || !session.isActive(now)) return Optional.empty();

        Long userId = Long.valueOf(claims.get("userId", String.class));
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !session.getUserId().equals(userId)
                || user.getUserStatus() == null || user.getUserStatus() != Constants.ACTIVE_USER_STATUS) {
            close(session, now);
            log.info("[Auth] Session {} closed on refresh: user {} is not active", sessionId, userId);
            return Optional.empty();
        }

        session.setLastRefreshedAt(now);
        session.setExpiresAt(now.plusSeconds(JwtUtil.refreshValiditySeconds()));
        sessionRepository.save(session);
        return Optional.of(issue(user, sessionId));
    }

    /** Logout: closes the session the refresh token belongs to. Unknown or invalid tokens are ignored. */
    @Transactional
    public void close(String refreshToken) {
        Claims claims = JwtUtil.parseRefreshToken(refreshToken);
        String sessionId = claims == null ? null : claims.get(JwtUtil.SESSION_CLAIM, String.class);
        if (sessionId == null) return;
        sessionRepository.findById(sessionId).ifPresent(s -> close(s, LocalDateTime.now()));
    }

    /** True when an access token with this session id may still be used. */
    public boolean isActive(String sessionId) {
        return activeCache.get(sessionId, id -> sessionRepository.findById(id)
                .map(s -> s.isActive(LocalDateTime.now()))
                .orElse(false));
    }

    private void close(AuthSession session, LocalDateTime now) {
        if (session.getRevokedAt() == null) {
            session.setRevokedAt(now);
            sessionRepository.save(session);
        }
        activeCache.invalidate(session.getId());
    }

    private Tokens issue(User user, String sessionId) {
        var authorities = new UserDetailsImpl(user).getAuthorities();
        String userId = String.valueOf(user.getId());
        return new Tokens(JwtUtil.createToken(userId, authorities, sessionId),
                JwtUtil.createRefreshToken(userId, authorities, sessionId));
    }
}
