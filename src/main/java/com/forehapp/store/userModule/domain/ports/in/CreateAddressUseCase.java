package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.AddressResponse;
import com.forehapp.store.userModule.application.dto.CreateAddressDto;

public interface CreateAddressUseCase {
    AddressResponse createAddress(Long userId, CreateAddressDto dto);
}
