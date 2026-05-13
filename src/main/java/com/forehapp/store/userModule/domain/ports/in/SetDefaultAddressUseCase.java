package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.AddressResponse;

public interface SetDefaultAddressUseCase {
    AddressResponse setDefault(Long userId, Long addressId);
}
