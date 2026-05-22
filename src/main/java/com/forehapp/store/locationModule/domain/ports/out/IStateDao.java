package com.forehapp.store.locationModule.domain.ports.out;

import com.forehapp.store.locationModule.domain.model.State;

import java.util.List;
import java.util.Optional;

public interface IStateDao {
    State save(State state);
    Optional<State> findById(Long id);
    List<State> findActiveByCountryId(Long countryId);
    void delete(State state);
}
