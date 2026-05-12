package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.Line;

public record LineResponse(Long id, String name, Long categoryId) {
    public LineResponse(Line line) {
        this(line.getId(), line.getDescription(), line.getCategory().getId());
    }
}
