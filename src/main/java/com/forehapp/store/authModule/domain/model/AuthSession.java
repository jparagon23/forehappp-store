package com.forehapp.store.authModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One login (one device). Its id travels in the tokens; closing it ends that login. */
@Entity
@Table(name = "store_auth_sessions")
@Getter @Setter
@NoArgsConstructor
public class AuthSession {

    @Id
    @Column(name = "session_id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastRefreshedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    /**
     * Hash of the user's password hash when the session opened. The password can also change in ForehApp
     * (same users table); a different value means it changed, and the session ends. Null = not recorded yet.
     */
    @Column(length = 64)
    private String passwordFingerprint;

    public boolean isActive(LocalDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
