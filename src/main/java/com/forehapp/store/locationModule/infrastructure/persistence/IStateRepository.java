package com.forehapp.store.locationModule.infrastructure.persistence;

import com.forehapp.store.locationModule.domain.model.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IStateRepository extends JpaRepository<State, Long> {
    List<State> findByCountryIdAndActiveTrue(Long countryId);
}
