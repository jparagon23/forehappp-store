package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.orderModule.infrastructure.web.dto.SellerOrderGroupDto;

import java.util.List;

public interface IOrderModuleService {
    List<SellerOrderGroupDto> getSellerGroups(Long userId);
    SellerOrderGroupDto getSellerGroupById(Long userId, Long groupId);
    void prepareGroup(Long userId, Long groupId);
    void shipGroup(Long userId, Long groupId, String trackingNumber);
    void deliverGroup(Long userId, Long groupId);
    void cancelGroup(Long userId, Long groupId, String reason);
}
