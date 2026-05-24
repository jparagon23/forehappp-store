package com.forehapp.store.locationModule.domain.ports.out;

import com.forehapp.store.locationModule.domain.model.Country;

import java.util.List;
import java.util.Optional;

public interface ICountryDao {
    Country save(Country country);
    Optional<Country> findById(Long id);
    List<Country> findAllActive();
    void delete(Country country);
}
