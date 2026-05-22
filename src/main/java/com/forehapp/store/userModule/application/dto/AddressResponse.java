package com.forehapp.store.userModule.application.dto;

import com.forehapp.store.locationModule.domain.model.City;
import com.forehapp.store.locationModule.infrastructure.web.dto.CityResponse;
import com.forehapp.store.locationModule.infrastructure.web.dto.CountryResponse;
import com.forehapp.store.locationModule.infrastructure.web.dto.StateResponse;
import com.forehapp.store.userModule.domain.model.UserAddress;
import lombok.Getter;

@Getter
public class AddressResponse {

    private final Long id;
    private final String alias;
    private final String street;
    private final CityResponse city;
    private final StateResponse state;
    private final CountryResponse country;
    private final String zipCode;
    private final Boolean isDefault;

    public AddressResponse(UserAddress address) {
        this.id = address.getId();
        this.alias = address.getAlias();
        this.street = address.getStreet();
        City c = address.getCity();
        this.city = CityResponse.from(c);
        this.state = StateResponse.from(c.getState());
        this.country = CountryResponse.from(c.getState().getCountry());
        this.zipCode = address.getZipCode();
        this.isDefault = address.getIsDefault();
    }
}
