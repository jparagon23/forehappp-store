package com.forehapp.store.ambassadorModule.infrastructure.persistence;

import com.forehapp.store.ambassadorModule.domain.model.AmbassadorCommission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAmbassadorCommissionJpaRepository extends JpaRepository<AmbassadorCommission, Long> {
    List<AmbassadorCommission> findByAmbassadorIdOrderByCreatedAtDesc(Long ambassadorId);
}
