package com.forehapp.store.shippingModule.domain.ports.in;

import com.forehapp.store.shippingModule.infrastructure.web.dto.CreateShippingZoneDto;
import com.forehapp.store.shippingModule.infrastructure.web.dto.ShippingZoneResponse;
import com.forehapp.store.shippingModule.infrastructure.web.dto.UpdateShippingZoneDto;

import java.util.List;

public interface IShippingZoneService {
    ShippingZoneResponse create(CreateShippingZoneDto dto, Long userId);
    ShippingZoneResponse update(Long zoneId, UpdateShippingZoneDto dto, Long userId);
    ShippingZoneResponse getById(Long zoneId, Long userId);
    List<ShippingZoneResponse> getAll(Long userId);
    void delete(Long zoneId, Long userId);
}
