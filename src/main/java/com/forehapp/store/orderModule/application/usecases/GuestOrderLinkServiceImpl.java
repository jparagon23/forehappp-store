package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.locationModule.domain.ports.out.ICityDao;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.ports.in.IGuestOrderLinkService;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.UserAddress;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.IUserAddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class GuestOrderLinkServiceImpl implements IGuestOrderLinkService {

    private static final Logger log = LoggerFactory.getLogger(GuestOrderLinkServiceImpl.class);

    private final IOrderDao orderDao;
    private final IStoreProfileDao storeProfileDao;
    private final IUserAddressRepository addressRepository;
    private final ICityDao cityDao;

    public GuestOrderLinkServiceImpl(IOrderDao orderDao,
                                     IStoreProfileDao storeProfileDao,
                                     IUserAddressRepository addressRepository,
                                     ICityDao cityDao) {
        this.orderDao = orderDao;
        this.storeProfileDao = storeProfileDao;
        this.addressRepository = addressRepository;
        this.cityDao = cityDao;
    }

    @Override
    @Transactional
    public int linkGuestOrders(StoreProfile profile) {
        String email = profile.getUser().getEmail();
        if (email == null || email.isBlank()) return 0;

        List<Order> guestOrders = orderDao.findGuestOrdersByEmail(email.trim().toLowerCase(Locale.ROOT));
        if (guestOrders.isEmpty()) return 0;

        // Ordered newest first: the latest order has the most current contact data
        Order latest = guestOrders.get(0);
        if ((profile.getPhone() == null || profile.getPhone().isBlank()) && latest.getBuyerPhone() != null) {
            profile.setPhone(latest.getBuyerPhone());
            storeProfileDao.save(profile);
        }
        if (profile.getAddresses().isEmpty() && latest.getShippingCityId() != null) {
            cityDao.findById(latest.getShippingCityId()).ifPresent(city -> {
                UserAddress address = new UserAddress();
                address.setStoreProfile(profile);
                address.setStreet(latest.getShippingAddress());
                address.setCity(city);
                address.setAlias("Dirección de envío");
                address.setIsDefault(true);
                addressRepository.save(address);
            });
        }

        guestOrders.forEach(order -> {
            order.setBuyer(profile);
            orderDao.save(order);
        });
        log.info("Linked {} guest order(s) to profile id={}", guestOrders.size(), profile.getId());
        return guestOrders.size();
    }
}
