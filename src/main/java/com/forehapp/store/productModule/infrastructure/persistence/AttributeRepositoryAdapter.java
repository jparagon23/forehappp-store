package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Attribute;
import com.forehapp.store.productModule.domain.ports.out.IAttributeDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AttributeRepositoryAdapter implements IAttributeDao {

    private final IAttributeJpaRepository jpaRepository;

    public AttributeRepositoryAdapter(IAttributeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Attribute> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Attribute save(Attribute attribute) {
        return jpaRepository.save(attribute);
    }
}
