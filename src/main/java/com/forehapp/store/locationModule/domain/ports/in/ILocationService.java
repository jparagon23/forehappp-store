package com.forehapp.store.locationModule.domain.ports.in;

import com.forehapp.store.locationModule.infrastructure.web.dto.*;

import java.util.List;

public interface ILocationService {
    // Countries
    CountryResponse createCountry(CreateCountryDto dto, Long userId);
    List<CountryResponse> getCountries();

    // States
    StateResponse createState(CreateStateDto dto, Long userId);
    List<StateResponse> getStatesByCountry(Long countryId);

    // Cities
    CityResponse createCity(CreateCityDto dto, Long userId);
    List<CityResponse> getCitiesByState(Long stateId);
}
