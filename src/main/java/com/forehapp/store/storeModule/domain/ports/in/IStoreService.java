package com.forehapp.store.storeModule.domain.ports.in;

import com.forehapp.store.storeModule.application.dto.*;

import java.util.List;

public interface IStoreService {
    StoreResponse createStore(CreateStoreRequestDto dto, Long userId);
    List<MyStoreResponse> getMyStores(Long userId);
    StoreResponse getStore(Long storeId, Long userId);
    StoreResponse updateStore(Long storeId, UpdateStoreRequestDto dto, Long userId);
    StoreMembershipResponse inviteMember(Long storeId, InviteMemberRequestDto dto, Long userId);
    StoreMembershipResponse updateMemberRole(Long storeId, Long membershipId, UpdateMemberRoleRequestDto dto, Long userId);
    void removeMember(Long storeId, Long membershipId, Long userId);
    List<StoreMembershipResponse> getMembers(Long storeId, Long userId);
}
