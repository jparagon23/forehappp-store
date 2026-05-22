package com.forehapp.store.locationModule.infrastructure.persistence;

import com.forehapp.store.locationModule.domain.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICityRepository extends JpaRepository<City, Long> {
    List<City> findByStateIdAndActiveTrue(Long stateId);
    List<City> findByIdIn(List<Long> ids);
}
