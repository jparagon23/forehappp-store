package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.Attribute;

import java.util.List;
import java.util.Optional;

public interface IAttributeDao {
    List<Attribute> findAll();
    Optional<Attribute> findById(Long id);
    Attribute save(Attribute attribute);
    void delete(Attribute attribute);
    boolean hasValues(Long attributeId);
}
