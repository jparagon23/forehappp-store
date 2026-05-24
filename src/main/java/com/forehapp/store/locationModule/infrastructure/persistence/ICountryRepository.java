package com.forehapp.store.locationModule.infrastructure.persistence;

import com.forehapp.store.locationModule.domain.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICountryRepository extends JpaRepository<Country, Long> {
    List<Country> findByActiveTrue();
}
