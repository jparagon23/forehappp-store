package com.forehapp.store.shippingModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.shippingModule.domain.model.ShippingZone;
import com.forehapp.store.shippingModule.domain.ports.in.IShippingZoneService;
import com.forehapp.store.shippingModule.domain.ports.out.IShippingZoneDao;
import com.forehapp.store.shippingModule.infrastructure.web.dto.CreateShippingZoneDto;
import com.forehapp.store.shippingModule.infrastructure.web.dto.ShippingZoneResponse;
import com.forehapp.store.shippingModule.infrastructure.web.dto.UpdateShippingZoneDto;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShippingZoneServiceImpl implements IShippingZoneService {

    private final IShippingZoneDao zoneDao;
    private final IStoreProfileDao storeProfileDao;

    public ShippingZoneServiceImpl(IShippingZoneDao zoneDao, IStoreProfileDao storeProfileDao) {
        this.zoneDao = zoneDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public ShippingZoneResponse create(CreateShippingZoneDto dto, Long userId) {
        requireAdmin(userId);
        boolean wantsDefault = Boolean.TRUE.equals(dto.isDefault());
        if (wantsDefault && zoneDao.existsAnotherDefault(0L)) {
            throw new BadRequestException(ErrorCode.SHIPPING_ZONE_DEFAULT_DUPLICATE,
                    "There is already an active default shipping zone");
        }
        ShippingZone zone = new ShippingZone();
        zone.setName(dto.name().trim());
        zone.setCities(dto.cities() != null ? new ArrayList<>(dto.cities()) : new ArrayList<>());
        zone.setCost(dto.cost());
        zone.setIsDefault(wantsDefault);
        return ShippingZoneResponse.from(zoneDao.save(zone));
    }

    @Override
    @Transactional
    public ShippingZoneResponse update(Long zoneId, UpdateShippingZoneDto dto, Long userId) {
        requireAdmin(userId);
        ShippingZone zone = requireZone(zoneId);
        if (dto.name() != null) zone.setName(dto.name().trim());
        if (dto.cities() != null) zone.setCities(new ArrayList<>(dto.cities()));
        if (dto.cost() != null) zone.setCost(dto.cost());
        if (dto.active() != null) zone.setActive(dto.active());
        if (dto.isDefault() != null) {
            if (Boolean.TRUE.equals(dto.isDefault()) && zoneDao.existsAnotherDefault(zoneId)) {
                throw new BadRequestException(ErrorCode.SHIPPING_ZONE_DEFAULT_DUPLICATE,
                        "There is already an active default shipping zone");
            }
            zone.setIsDefault(dto.isDefault());
        }
        return ShippingZoneResponse.from(zoneDao.save(zone));
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingZoneResponse getById(Long zoneId, Long userId) {
        requireAdmin(userId);
        return ShippingZoneResponse.from(requireZone(zoneId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingZoneResponse> getAll(Long userId) {
        requireAdmin(userId);
        return zoneDao.findAll().stream().map(ShippingZoneResponse::from).toList();
    }

    @Override
    @Transactional
    public void delete(Long zoneId, Long userId) {
        requireAdmin(userId);
        zoneDao.delete(requireZone(zoneId));
    }

    private ShippingZone requireZone(Long zoneId) {
        return zoneDao.findById(zoneId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SHIPPING_ZONE_NOT_FOUND, "Shipping zone not found"));
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Admin access required");
        }
    }
}
