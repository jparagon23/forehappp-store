package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.orderModule.infrastructure.web.dto.SellerOrderGroupDto;

import java.util.List;

public interface IOrderModuleService {
    List<SellerOrderGroupDto> getSellerGroups(Long storeId, Long userId);
    SellerOrderGroupDto getSellerGroupById(Long storeId, Long groupId, Long userId);
    void prepareGroup(Long storeId, Long groupId, Long userId);
    void shipGroup(Long storeId, Long groupId, String trackingNumber, Long userId);
    void deliverGroup(Long storeId, Long groupId, Long userId);
    void cancelGroup(Long storeId, Long groupId, String reason, Long userId);
}
