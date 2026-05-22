package com.forehapp.store.shippingModule.infrastructure.web.dto;

import com.forehapp.store.shippingModule.domain.model.ShippingZone;

import java.math.BigDecimal;
import java.util.List;

public record ShippingZoneResponse(
        Long id,
        String name,
        List<String> cities,
        BigDecimal cost,
        Boolean isDefault,
        Boolean active
) {
    public static ShippingZoneResponse from(ShippingZone zone) {
        return new ShippingZoneResponse(
                zone.getId(),
                zone.getName(),
                List.copyOf(zone.getCities()),
                zone.getCost(),
                zone.getIsDefault(),
                zone.getActive()
        );
    }
}
