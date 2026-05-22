package com.forehapp.store.shippingModule.infrastructure.web.dto;

import com.forehapp.store.locationModule.infrastructure.web.dto.CityResponse;
import com.forehapp.store.shippingModule.domain.model.ShippingZone;

import java.math.BigDecimal;
import java.util.List;

public record ShippingZoneResponse(
        Long id,
        String name,
        List<CityResponse> cities,
        BigDecimal cost,
        Boolean isDefault,
        Boolean active
) {
    public static ShippingZoneResponse from(ShippingZone zone) {
        return new ShippingZoneResponse(
                zone.getId(),
                zone.getName(),
                zone.getCities().stream().map(CityResponse::from).toList(),
                zone.getCost(),
                zone.getIsDefault(),
                zone.getActive()
        );
    }
}
