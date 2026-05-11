package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.AttributeValue;

import java.util.List;

public interface IAttributeValueDao {
    List<AttributeValue> findAllByIds(List<Long> ids);
}
