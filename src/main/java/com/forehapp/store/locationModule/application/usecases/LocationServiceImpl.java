package com.forehapp.store.locationModule.application.usecases;

import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.locationModule.domain.model.City;
import com.forehapp.store.locationModule.domain.model.Country;
import com.forehapp.store.locationModule.domain.model.State;
import com.forehapp.store.locationModule.domain.ports.in.ILocationService;
import com.forehapp.store.locationModule.domain.ports.out.ICityDao;
import com.forehapp.store.locationModule.domain.ports.out.ICountryDao;
import com.forehapp.store.locationModule.domain.ports.out.IStateDao;
import com.forehapp.store.locationModule.infrastructure.web.dto.*;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationServiceImpl implements ILocationService {

    private final ICountryDao countryDao;
    private final IStateDao stateDao;
    private final ICityDao cityDao;
    private final IStoreProfileDao storeProfileDao;

    public LocationServiceImpl(ICountryDao countryDao, IStateDao stateDao,
                               ICityDao cityDao, IStoreProfileDao storeProfileDao) {
        this.countryDao = countryDao;
        this.stateDao = stateDao;
        this.cityDao = cityDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public CountryResponse createCountry(CreateCountryDto dto, Long userId) {
        requireAdmin(userId);
        Country country = new Country();
        country.setName(dto.name().trim());
        country.setCode(dto.code().toUpperCase().trim());
        return CountryResponse.from(countryDao.save(country));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryResponse> getCountries() {
        return countryDao.findAllActive().stream().map(CountryResponse::from).toList();
    }

    @Override
    @Transactional
    public StateResponse createState(CreateStateDto dto, Long userId) {
        requireAdmin(userId);
        Country country = countryDao.findById(dto.countryId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOCATION_COUNTRY_NOT_FOUND, "Country not found"));
        State state = new State();
        state.setName(dto.name().trim());
        state.setCountry(country);
        return StateResponse.from(stateDao.save(state));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StateResponse> getStatesByCountry(Long countryId) {
        return stateDao.findActiveByCountryId(countryId).stream().map(StateResponse::from).toList();
    }

    @Override
    @Transactional
    public CityResponse createCity(CreateCityDto dto, Long userId) {
        requireAdmin(userId);
        State state = stateDao.findById(dto.stateId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOCATION_STATE_NOT_FOUND, "State not found"));
        City city = new City();
        city.setName(dto.name().trim());
        city.setState(state);
        return CityResponse.from(cityDao.save(city));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> getCitiesByState(Long stateId) {
        return cityDao.findActiveByStateId(stateId).stream().map(CityResponse::from).toList();
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Admin access required");
        }
    }
}
