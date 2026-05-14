package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.UserAddress;
import com.forehapp.store.userModule.domain.ports.out.IUserAddressRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserAddressRepositoryAdapter implements IUserAddressRepository {

    private final IUserAddressJpaRepository jpaRepository;

    public UserAddressRepositoryAdapter(IUserAddressJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<UserAddress> findByStoreProfileId(Long storeProfileId) {
        return jpaRepository.findByStoreProfileId(storeProfileId);
    }

    @Override
    public Optional<UserAddress> findById(Long addressId) {
        return jpaRepository.findById(addressId);
    }

    @Override
    public Optional<UserAddress> findDefaultByStoreProfileId(Long storeProfileId) {
        return jpaRepository.findByStoreProfileIdAndIsDefaultTrue(storeProfileId);
    }

    @Override
    public UserAddress save(UserAddress address) {
        return jpaRepository.save(address);
    }

    @Override
    public void delete(UserAddress address) {
        jpaRepository.delete(address);
    }

    @Override
    public void clearDefaultForProfile(Long storeProfileId) {
        jpaRepository.clearDefaultForProfile(storeProfileId);
    }
}
