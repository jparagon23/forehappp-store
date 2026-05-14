package com.forehapp.store.userModule.application.dto;

import com.forehapp.store.userModule.domain.model.UserAddress;
import lombok.Getter;

@Getter
public class AddressResponse {

    private final Long id;
    private final String alias;
    private final String street;
    private final String city;
    private final String state;
    private final String country;
    private final String zipCode;
    private final Boolean isDefault;

    public AddressResponse(UserAddress address) {
        this.id = address.getId();
        this.alias = address.getAlias();
        this.street = address.getStreet();
        this.city = address.getCity();
        this.state = address.getState();
        this.country = address.getCountry();
        this.zipCode = address.getZipCode();
        this.isDefault = address.getIsDefault();
    }
}
