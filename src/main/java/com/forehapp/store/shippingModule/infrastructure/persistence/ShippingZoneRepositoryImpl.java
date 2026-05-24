package com.forehapp.store.shippingModule.infrastructure.persistence;

import com.forehapp.store.shippingModule.domain.model.ShippingZone;
import com.forehapp.store.shippingModule.domain.ports.out.IShippingZoneDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ShippingZoneRepositoryImpl implements IShippingZoneDao {

    private final IShippingZoneRepository jpaRepository;

    public ShippingZoneRepositoryImpl(IShippingZoneRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override public ShippingZone save(ShippingZone zone) { return jpaRepository.save(zone); }
    @Override public Optional<ShippingZone> findById(Long id) { return jpaRepository.findById(id); }
    @Override public List<ShippingZone> findAll() { return jpaRepository.findAll(); }
    @Override public Optional<ShippingZone> findActiveByCityId(Long cityId) { return jpaRepository.findActiveByCityId(cityId); }
    @Override public Optional<ShippingZone> findActiveDefault() { return jpaRepository.findActiveDefault(); }
    @Override public boolean existsAnotherDefault(Long excludeId) { return jpaRepository.existsAnotherDefault(excludeId); }
    @Override public void delete(ShippingZone zone) { jpaRepository.delete(zone); }
}
