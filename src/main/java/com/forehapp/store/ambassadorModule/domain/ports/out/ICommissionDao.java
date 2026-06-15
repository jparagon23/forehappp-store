package com.forehapp.store.ambassadorModule.domain.ports.out;

import com.forehapp.store.ambassadorModule.domain.model.AmbassadorCommission;

import java.util.List;
import java.util.Optional;

public interface ICommissionDao {
    Optional<AmbassadorCommission> findById(Long id);
    List<AmbassadorCommission> findByAmbassadorId(Long ambassadorId);
    AmbassadorCommission save(AmbassadorCommission commission);
}
