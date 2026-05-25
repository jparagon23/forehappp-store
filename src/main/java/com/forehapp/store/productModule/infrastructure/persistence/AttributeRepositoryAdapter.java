package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Attribute;
import com.forehapp.store.productModule.domain.ports.out.IAttributeDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AttributeRepositoryAdapter implements IAttributeDao {

    private final IAttributeJpaRepository jpaRepository;
    private final IAttributeValueJpaRepository attributeValueJpaRepository;

    public AttributeRepositoryAdapter(IAttributeJpaRepository jpaRepository,
                                      IAttributeValueJpaRepository attributeValueJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.attributeValueJpaRepository = attributeValueJpaRepository;
    }

    @Override
    public List<Attribute> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Attribute> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Attribute save(Attribute attribute) {
        return jpaRepository.save(attribute);
    }

    @Override
    public void delete(Attribute attribute) {
        jpaRepository.delete(attribute);
    }

    @Override
    public boolean hasValues(Long attributeId) {
        return attributeValueJpaRepository.existsByAttributeId(attributeId);
    }
}
