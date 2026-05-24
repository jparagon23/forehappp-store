package com.forehapp.store.locationModule.infrastructure.web.dto;

import com.forehapp.store.locationModule.domain.model.City;

public record CityResponse(Long id, String name, Long stateId) {
    public static CityResponse from(City c) {
        return new CityResponse(c.getId(), c.getName(), c.getState().getId());
    }
}
