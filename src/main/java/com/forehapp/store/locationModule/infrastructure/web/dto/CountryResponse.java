package com.forehapp.store.locationModule.infrastructure.web.dto;

import com.forehapp.store.locationModule.domain.model.Country;

public record CountryResponse(Long id, String name, String code) {
    public static CountryResponse from(Country c) {
        return new CountryResponse(c.getId(), c.getName(), c.getCode());
    }
}
