package com.forehapp.store.authModule.infrastructure.persistence;

import com.forehapp.store.authModule.domain.model.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConfirmationTokenJpaRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE confirmation_token SET confirmed_at = :now WHERE token = :token")
    void setConfirmedAt(String token, LocalDateTime now);
}
