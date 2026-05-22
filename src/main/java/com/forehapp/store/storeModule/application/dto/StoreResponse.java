package com.forehapp.store.storeModule.application.dto;

import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.storeModule.domain.model.StoreStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class StoreResponse {

    private final Long id;
    private final String name;
    private final String slug;
    private final String description;
    private final StoreStatus status;
    private final BigDecimal freeShippingMinAmount;
    private final LocalDateTime createdAt;

    public StoreResponse(Store store) {
        this.id = store.getId();
        this.name = store.getName();
        this.slug = store.getSlug();
        this.description = store.getDescription();
        this.status = store.getStatus();
        this.freeShippingMinAmount = store.getFreeShippingMinAmount();
        this.createdAt = store.getCreatedAt();
    }
}
