package com.forehapp.store.userModule.domain.ports.in;

public interface DeleteAddressUseCase {
    void deleteAddress(Long userId, Long addressId);
}
