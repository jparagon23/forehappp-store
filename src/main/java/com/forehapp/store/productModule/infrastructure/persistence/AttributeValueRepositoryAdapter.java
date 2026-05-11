package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.AttributeValue;
import com.forehapp.store.productModule.domain.ports.out.IAttributeValueDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AttributeValueRepositoryAdapter implements IAttributeValueDao {

    private final IAttributeValueJpaRepository jpaRepository;

    public AttributeValueRepositoryAdapter(IAttributeValueJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<AttributeValue> findAllByIds(List<Long> ids) {
        return jpaRepository.findByIdInWithAttribute(ids);
    }
}
