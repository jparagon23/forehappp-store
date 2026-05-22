package com.forehapp.store.locationModule.domain.ports.out;

import com.forehapp.store.locationModule.domain.model.City;

import java.util.List;
import java.util.Optional;

public interface ICityDao {
    City save(City city);
    Optional<City> findById(Long id);
    List<City> findActiveByStateId(Long stateId);
    List<City> findAllByIds(List<Long> ids);
    void delete(City city);
}
