package com.forehapp.store.userModule.domain.ports.out;

import com.forehapp.store.userModule.domain.model.UserAddress;

import java.util.List;
import java.util.Optional;

public interface IUserAddressRepository {
    List<UserAddress> findByStoreProfileId(Long storeProfileId);
    Optional<UserAddress> findById(Long addressId);
    Optional<UserAddress> findDefaultByStoreProfileId(Long storeProfileId);
    UserAddress save(UserAddress address);
    void delete(UserAddress address);
    void clearDefaultForProfile(Long storeProfileId);
}
