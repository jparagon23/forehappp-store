package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.Attribute;

import java.util.Optional;

public interface IAttributeDao {
    Optional<Attribute> findById(Long id);
    Attribute save(Attribute attribute);
}
