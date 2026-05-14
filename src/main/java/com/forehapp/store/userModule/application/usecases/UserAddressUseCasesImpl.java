package com.forehapp.store.userModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.userModule.application.dto.AddressResponse;
import com.forehapp.store.userModule.application.dto.CreateAddressDto;
import com.forehapp.store.userModule.application.dto.UpdateAddressDto;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.UserAddress;
import com.forehapp.store.userModule.domain.ports.in.*;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.IUserAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressUseCasesImpl implements
        GetAddressesUseCase,
        CreateAddressUseCase,
        UpdateAddressUseCase,
        DeleteAddressUseCase,
        SetDefaultAddressUseCase {

    private final IUserAddressRepository addressRepository;
    private final IStoreProfileDao storeProfileDao;

    public UserAddressUseCasesImpl(IUserAddressRepository addressRepository,
                                   IStoreProfileDao storeProfileDao) {
        this.addressRepository = addressRepository;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    public List<AddressResponse> getAddresses(Long userId) {
        StoreProfile profile = findProfileOrThrow(userId);
        return addressRepository.findByStoreProfileId(profile.getId())
                .stream()
                .map(AddressResponse::new)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse createAddress(Long userId, CreateAddressDto dto) {
        StoreProfile profile = findProfileOrThrow(userId);

        List<UserAddress> existing = addressRepository.findByStoreProfileId(profile.getId());
        if (existing.size() >= 10) {
            throw new BadRequestException("Maximum of 10 addresses allowed per profile");
        }

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            addressRepository.clearDefaultForProfile(profile.getId());
        }

        boolean isFirstAddress = existing.isEmpty();

        UserAddress address = new UserAddress();
        address.setStoreProfile(profile);
        address.setAlias(dto.getAlias());
        address.setStreet(dto.getStreet().trim());
        address.setCity(dto.getCity().trim());
        address.setState(dto.getState() != null ? dto.getState().trim() : null);
        address.setCountry(dto.getCountry().trim());
        address.setZipCode(dto.getZipCode() != null ? dto.getZipCode().trim() : null);
        address.setIsDefault(isFirstAddress || Boolean.TRUE.equals(dto.getIsDefault()));

        return new AddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressDto dto) {
        UserAddress address = findAddressForUser(userId, addressId);

        if (dto.getAlias() != null) address.setAlias(dto.getAlias().trim());
        if (dto.getStreet() != null) address.setStreet(dto.getStreet().trim());
        if (dto.getCity() != null) address.setCity(dto.getCity().trim());
        if (dto.getState() != null) address.setState(dto.getState().trim());
        if (dto.getCountry() != null) address.setCountry(dto.getCountry().trim());
        if (dto.getZipCode() != null) address.setZipCode(dto.getZipCode().trim());

        return new AddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = findAddressForUser(userId, addressId);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            throw new BadRequestException("Cannot delete the default address. Set another address as default first");
        }

        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressResponse setDefault(Long userId, Long addressId) {
        UserAddress address = findAddressForUser(userId, addressId);

        addressRepository.clearDefaultForProfile(address.getStoreProfile().getId());
        address.setIsDefault(true);

        return new AddressResponse(addressRepository.save(address));
    }

    private StoreProfile findProfileOrThrow(Long userId) {
        return storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Store profile not found"));
    }

    private UserAddress findAddressForUser(Long userId, Long addressId) {
        StoreProfile profile = findProfileOrThrow(userId);
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (!address.getStoreProfile().getId().equals(profile.getId())) {
            throw new NotFoundException("Address not found");
        }

        return address;
    }
}
