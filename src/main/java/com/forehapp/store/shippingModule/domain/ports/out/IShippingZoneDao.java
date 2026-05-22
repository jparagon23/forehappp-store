package com.forehapp.store.shippingModule.domain.ports.out;

import com.forehapp.store.shippingModule.domain.model.ShippingZone;

import java.util.List;
import java.util.Optional;

public interface IShippingZoneDao {
    ShippingZone save(ShippingZone zone);
    Optional<ShippingZone> findById(Long id);
    List<ShippingZone> findAll();
    Optional<ShippingZone> findActiveByCityId(Long cityId);
    Optional<ShippingZone> findActiveDefault();
    boolean existsAnotherDefault(Long excludeId);
    void delete(ShippingZone zone);
}
