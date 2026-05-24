package com.forehapp.store.locationModule.infrastructure.persistence;

import com.forehapp.store.locationModule.domain.model.Country;
import com.forehapp.store.locationModule.domain.ports.out.ICountryDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CountryRepositoryImpl implements ICountryDao {

    private final ICountryRepository jpa;

    public CountryRepositoryImpl(ICountryRepository jpa) { this.jpa = jpa; }

    @Override public Country save(Country c) { return jpa.save(c); }
    @Override public Optional<Country> findById(Long id) { return jpa.findById(id); }
    @Override public List<Country> findAllActive() { return jpa.findByActiveTrue(); }
    @Override public void delete(Country c) { jpa.delete(c); }
}
