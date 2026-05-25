package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.AttributeValue;

import java.util.List;
import java.util.Optional;

public interface IAttributeValueDao {
    List<AttributeValue> findAllByIds(List<Long> ids);
    List<AttributeValue> findAllByAttributeIds(List<Long> attributeIds);
    Optional<AttributeValue> findByIdAndAttributeId(Long id, Long attributeId);
    AttributeValue save(AttributeValue attributeValue);
    void delete(AttributeValue attributeValue);
    boolean isUsedByVariants(Long valueId);
}
