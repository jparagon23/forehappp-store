package com.forehapp.store.locationModule.infrastructure.persistence;

import com.forehapp.store.locationModule.domain.model.State;
import com.forehapp.store.locationModule.domain.ports.out.IStateDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StateRepositoryImpl implements IStateDao {

    private final IStateRepository jpa;

    public StateRepositoryImpl(IStateRepository jpa) { this.jpa = jpa; }

    @Override public State save(State s) { return jpa.save(s); }
    @Override public Optional<State> findById(Long id) { return jpa.findById(id); }
    @Override public List<State> findActiveByCountryId(Long countryId) { return jpa.findByCountryIdAndActiveTrue(countryId); }
    @Override public void delete(State s) { jpa.delete(s); }
}
