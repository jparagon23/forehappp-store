package com.forehapp.store.storeModule.application.dto;

import com.forehapp.store.storeModule.domain.model.StoreMembership;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoreMembershipResponse {

    private final Long membershipId;
    private final Long storeId;
    private final String storeName;
    private final Long storeProfileId;
    private final String memberName;
    private final String memberEmail;
    private final StoreMemberRole role;
    private final LocalDateTime joinedAt;
    private final Boolean active;

    public StoreMembershipResponse(StoreMembership membership) {
        this.membershipId = membership.getId();
        this.storeId = membership.getStore().getId();
        this.storeName = membership.getStore().getName();
        this.storeProfileId = membership.getStoreProfile().getId();
        this.memberName = membership.getStoreProfile().getUser().getName()
                + " " + membership.getStoreProfile().getUser().getLastname();
        this.memberEmail = membership.getStoreProfile().getUser().getEmail();
        this.role = membership.getRole();
        this.joinedAt = membership.getJoinedAt();
        this.active = membership.getActive();
    }
}
