package com.forehapp.store.ambassadorModule.infrastructure.persistence;

import com.forehapp.store.ambassadorModule.domain.model.AmbassadorCommission;
import com.forehapp.store.ambassadorModule.domain.ports.out.ICommissionDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommissionRepositoryImpl implements ICommissionDao {

    private final IAmbassadorCommissionJpaRepository jpaRepository;

    public CommissionRepositoryImpl(IAmbassadorCommissionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AmbassadorCommission> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<AmbassadorCommission> findByAmbassadorId(Long ambassadorId) {
        return jpaRepository.findByAmbassadorIdOrderByCreatedAtDesc(ambassadorId);
    }

    @Override
    public AmbassadorCommission save(AmbassadorCommission commission) {
        return jpaRepository.save(commission);
    }
}
