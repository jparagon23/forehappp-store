package com.forehapp.store.storeModule.application.dto;

import com.forehapp.store.storeModule.domain.model.StoreMembership;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import com.forehapp.store.storeModule.domain.model.StoreStatus;
import lombok.Getter;

@Getter
public class MyStoreResponse {

    private final Long storeId;
    private final String name;
    private final String slug;
    private final StoreStatus status;
    private final StoreMemberRole myRole;

    public MyStoreResponse(StoreMembership membership) {
        this.storeId = membership.getStore().getId();
        this.name = membership.getStore().getName();
        this.slug = membership.getStore().getSlug();
        this.status = membership.getStore().getStatus();
        this.myRole = membership.getRole();
    }
}
