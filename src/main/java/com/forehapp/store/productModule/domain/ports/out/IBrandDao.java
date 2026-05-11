package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.Brand;

import java.util.Optional;

public interface IBrandDao {
    Optional<Brand> findById(Long id);
}
