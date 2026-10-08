package com.forehapp.store.authModule.infrastructure.persistence;

import com.forehapp.store.authModule.domain.model.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfirmationTokenJpaRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findFirstByUser_IdAndConfirmedAtIsNullOrderByIdDesc(Long userId);

    List<ConfirmationToken> findByUser_IdAndConfirmedAtIsNull(Long userId);
}
