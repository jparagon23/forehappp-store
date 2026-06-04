package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.authModule.application.dto.LoginResponseDto;
import com.forehapp.store.general.constants.Constants;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.locationModule.domain.ports.out.ICityDao;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestCreateAccountRequestDto;
import com.forehapp.store.security.config.UserDetailsImpl;
import com.forehapp.store.security.jwt.JwtUtil;
import com.forehapp.store.userModule.domain.model.Role;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.model.UserAddress;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.IUserAddressRepository;
import com.forehapp.store.userModule.domain.ports.out.RoleRepository;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuestCreateAccountServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(GuestCreateAccountServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IStoreProfileDao storeProfileDao;
    private final IOrderDao orderDao;
    private final IUserAddressRepository addressRepository;
    private final ICityDao cityDao;

    public GuestCreateAccountServiceImpl(UserRepository userRepository,
                                         RoleRepository roleRepository,
                                         PasswordEncoder passwordEncoder,
                                         IStoreProfileDao storeProfileDao,
                                         IOrderDao orderDao,
                                         IUserAddressRepository addressRepository,
                                         ICityDao cityDao) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.storeProfileDao = storeProfileDao;
        this.orderDao = orderDao;
        this.addressRepository = addressRepository;
        this.cityDao = cityDao;
    }

    @Transactional
    public LoginResponseDto createAccount(GuestCreateAccountRequestDto dto) {
        String email = dto.email().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException(ErrorCode.AUTH_EMAIL_ALREADY_REGISTERED,
                    "An account already exists for this email");
        }

        List<Order> guestOrders = orderDao.findGuestOrdersByEmail(email);

        Order latestOrder = guestOrders.isEmpty() ? null : guestOrders.get(0);

        Role userRole = roleRepository.findById((long) Constants.USER_ROLE_ID)
                .orElseThrow(() -> new IllegalStateException("Default user role not found"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setName(latestOrder != null ? latestOrder.getGuestName() : "");
        user.setLastname(latestOrder != null ? latestOrder.getGuestLastname() : "");
        user.setUserStatus(Constants.ACTIVE_USER_STATUS);
        user.setAllowNotification("T");
        user.setRoles(List.of(userRole));
        User savedUser = userRepository.save(user);
        log.info("Guest account created for email={}", email);

        StoreProfile profile = new StoreProfile();
        profile.setUser(savedUser);
        profile.getRoles().add(StoreRole.CUSTOMER);
        if (latestOrder != null) {
            profile.setPhone(latestOrder.getBuyerPhone());
        }
        StoreProfile savedProfile = storeProfileDao.save(profile);

        if (latestOrder != null && latestOrder.getShippingCityId() != null) {
            cityDao.findById(latestOrder.getShippingCityId()).ifPresent(city -> {
                UserAddress address = new UserAddress();
                address.setStoreProfile(savedProfile);
                address.setStreet(latestOrder.getShippingAddress());
                address.setCity(city);
                address.setAlias("Dirección de envío");
                address.setIsDefault(true);
                addressRepository.save(address);
            });
        }

        guestOrders.forEach(order -> {
            order.setBuyer(savedProfile);
            orderDao.save(order);
        });
        log.info("Linked {} guest order(s) to new profile id={}", guestOrders.size(), savedProfile.getId());

        UserDetailsImpl userDetails = new UserDetailsImpl(savedUser);
        String accessToken = JwtUtil.createToken(String.valueOf(savedUser.getId()), userDetails.getAuthorities());
        String refreshToken = JwtUtil.createRefreshToken(String.valueOf(savedUser.getId()), userDetails.getAuthorities());

        return new LoginResponseDto(accessToken, refreshToken, savedUser.getId(),
                savedUser.getName(), savedUser.getEmail(), savedProfile.getRoles());
    }
}
