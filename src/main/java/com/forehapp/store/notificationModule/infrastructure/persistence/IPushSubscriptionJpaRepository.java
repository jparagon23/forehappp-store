package com.forehapp.store.notificationModule.infrastructure.persistence;

import com.forehapp.store.notificationModule.domain.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPushSubscriptionJpaRepository extends JpaRepository<PushSubscription, Long> {
    Optional<PushSubscription> findByEndpoint(String endpoint);
    List<PushSubscription> findByUserIdAndActiveTrue(Long userId);
    boolean existsByUserIdAndActiveTrue(Long userId);
}
