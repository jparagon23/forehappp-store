package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.AddressResponse;

import java.util.List;

public interface GetAddressesUseCase {
    List<AddressResponse> getAddresses(Long userId);
}
