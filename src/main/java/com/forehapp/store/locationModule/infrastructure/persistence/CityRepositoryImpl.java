package com.forehapp.store.locationModule.infrastructure.persistence;

import com.forehapp.store.locationModule.domain.model.City;
import com.forehapp.store.locationModule.domain.ports.out.ICityDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CityRepositoryImpl implements ICityDao {

    private final ICityRepository jpa;

    public CityRepositoryImpl(ICityRepository jpa) { this.jpa = jpa; }

    @Override public City save(City c) { return jpa.save(c); }
    @Override public Optional<City> findById(Long id) { return jpa.findById(id); }
    @Override public List<City> findActiveByStateId(Long stateId) { return jpa.findByStateIdAndActiveTrue(stateId); }
    @Override public List<City> findAllByIds(List<Long> ids) { return jpa.findByIdIn(ids); }
    @Override public void delete(City c) { jpa.delete(c); }
}
