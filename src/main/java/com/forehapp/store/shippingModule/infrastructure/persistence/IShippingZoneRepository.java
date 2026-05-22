package com.forehapp.store.shippingModule.infrastructure.persistence;

import com.forehapp.store.shippingModule.domain.model.ShippingZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IShippingZoneRepository extends JpaRepository<ShippingZone, Long> {

    @Query("SELECT z FROM ShippingZone z JOIN z.cities c WHERE LOWER(c) = LOWER(:city) AND z.active = true")
    Optional<ShippingZone> findActiveByCityName(@Param("city") String city);

    @Query("SELECT z FROM ShippingZone z WHERE z.isDefault = true AND z.active = true")
    Optional<ShippingZone> findActiveDefault();

    @Query("SELECT COUNT(z) > 0 FROM ShippingZone z WHERE z.isDefault = true AND z.id <> :excludeId")
    boolean existsAnotherDefault(@Param("excludeId") Long excludeId);
}
