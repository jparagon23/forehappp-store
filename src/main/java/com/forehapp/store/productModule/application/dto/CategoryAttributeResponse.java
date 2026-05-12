package com.forehapp.store.productModule.application.dto;

import java.util.List;

public record CategoryAttributeResponse(
        Long attributeId,
        String name,
        boolean required,
        List<AttributeValueDto> values
) {
    public record AttributeValueDto(Long id, String description) {}
}
