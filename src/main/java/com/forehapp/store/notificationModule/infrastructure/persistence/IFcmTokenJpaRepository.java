package com.forehapp.store.notificationModule.infrastructure.persistence;

import com.forehapp.store.notificationModule.domain.model.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IFcmTokenJpaRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByToken(String token);
    List<FcmToken> findByUserIdAndActiveTrue(Long userId);
    boolean existsByUserIdAndActiveTrue(Long userId);
}
