package com.forehapp.store.locationModule.infrastructure.web.dto;

import com.forehapp.store.locationModule.domain.model.State;

public record StateResponse(Long id, String name, Long countryId) {
    public static StateResponse from(State s) {
        return new StateResponse(s.getId(), s.getName(), s.getCountry().getId());
    }
}
