package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.Attribute;

public record AttributeResponse(Long id, String name) {
    public AttributeResponse(Attribute attribute) {
        this(attribute.getId(), attribute.getDescription());
    }
}
