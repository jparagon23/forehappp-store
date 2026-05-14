package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.AddressResponse;
import com.forehapp.store.userModule.application.dto.UpdateAddressDto;

public interface UpdateAddressUseCase {
    AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressDto dto);
}
