package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.general.constants.Constants;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.orderModule.application.dto.PlaceOrderCommand;
import com.forehapp.store.orderModule.domain.model.OrderChannel;
import com.forehapp.store.orderModule.domain.ports.in.IAssistedOrderService;
import com.forehapp.store.orderModule.domain.ports.in.IGuestCheckoutService;
import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedCouponValidateDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedCustomerResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestOrderItemDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.forehapp.store.promotionModule.application.dto.CouponValidationResponse;
import com.forehapp.store.promotionModule.application.dto.ValidateCouponRequestDto;
import com.forehapp.store.promotionModule.domain.ports.in.IPromotionService;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import com.forehapp.store.storeModule.domain.ports.out.IStoreDao;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class AssistedOrderServiceImpl implements IAssistedOrderService {

    private static final Logger log = LoggerFactory.getLogger(AssistedOrderServiceImpl.class);

    private final IGuestCheckoutService checkoutService;
    private final IStoreMembershipDao membershipDao;
    private final IStoreDao storeDao;
    private final IProductVariantDao variantDao;
    private final UserRepository userRepository;
    private final IStoreProfileDao storeProfileDao;
    private final IPromotionService promotionService;

    public AssistedOrderServiceImpl(IGuestCheckoutService checkoutService,
                                    IStoreMembershipDao membershipDao,
                                    IStoreDao storeDao,
                                    IProductVariantDao variantDao,
                                    UserRepository userRepository,
                                    IStoreProfileDao storeProfileDao,
                                    IPromotionService promotionService) {
        this.checkoutService = checkoutService;
        this.membershipDao = membershipDao;
        this.storeDao = storeDao;
        this.variantDao = variantDao;
        this.userRepository = userRepository;
        this.storeProfileDao = storeProfileDao;
        this.promotionService = promotionService;
    }

    @Override
    @Transactional(readOnly = true)
    public AssistedCustomerResponse lookupCustomer(Long storeId, String email, Long userId) {
        requireStoreManager(storeId, userId);
        return findActiveUser(email)
                .map(u -> new AssistedCustomerResponse(true, shortName(u)))
                .orElse(new AssistedCustomerResponse(false, null));
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(Long storeId, AssistedOrderRequestDto dto, Long userId) {
        requireStoreManager(storeId, userId);

        if (!Boolean.TRUE.equals(dto.dataConsent())) {
            throw new BadRequestException(ErrorCode.ASSISTED_ORDER_CONSENT_REQUIRED,
                    "The customer must authorize the use of their personal data");
        }
        if (dto.alreadyPaid() && dto.paymentMethod() != PaymentMethod.CASH && dto.paymentMethod() != PaymentMethod.TRANSFER) {
            throw new BadRequestException(ErrorCode.ASSISTED_ORDER_ALREADY_PAID_METHOD,
                    "Only CASH or TRANSFER orders can be registered as already paid");
        }
        for (GuestOrderItemDto item : dto.items()) {
            boolean ownVariant = variantDao.findById(item.variantId())
                    .map(ProductVariant::getProduct)
                    .map(p -> p.getStore().getId().equals(storeId))
                    .orElse(false);
            if (!ownVariant) {
                throw new BadRequestException(ErrorCode.ASSISTED_ORDER_VARIANT_NOT_IN_STORE,
                        "Variant " + item.variantId() + " does not belong to this store");
            }
        }

        Store store = storeDao.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND, "Store not found"));

        // An email with an active account gets the order in that account; otherwise it stays a guest
        // order and is linked when the customer signs up and verifies the email
        StoreProfile buyer = findActiveUser(dto.email()).map(this::resolveProfile).orElse(null);

        PlaceOrderCommand command = new PlaceOrderCommand(
                dto.name().trim(), dto.lastname().trim(), dto.email(), dto.phone().trim(),
                dto.shippingAddress(), dto.shippingCityId(), dto.shippingComplement(), dto.shippingReference(),
                dto.items(), dto.paymentMethod(), normalizeCoupon(dto.couponCode()), storeId, null,
                buyer, OrderChannel.ASSISTED, userId, LocalDateTime.now(), dto.alreadyPaid(), store.getName());

        OrderResponse response = checkoutService.place(command);
        log.info("[AssistedOrder] storeId={} userId={} placed order for {} customer (alreadyPaid={})",
                storeId, userId, buyer != null ? "registered" : "guest", dto.alreadyPaid());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(Long storeId, AssistedCouponValidateDto dto, Long userId) {
        requireStoreManager(storeId, userId);
        ValidateCouponRequestDto request = new ValidateCouponRequestDto(
                normalizeCoupon(dto.code()), storeId, dto.orderAmount(), dto.shippingCost());

        // Same rules the order will be redeemed with: the account's when the email has one, else the guest's
        Optional<User> account = findActiveUser(dto.email())
                .filter(u -> storeProfileDao.findByUserId(u.getId()).isPresent());
        return account.isPresent()
                ? promotionService.validateCoupon(account.get().getId(), request)
                : promotionService.validateCouponAsGuest(dto.email().trim().toLowerCase(Locale.ROOT), request);
    }

    private static String normalizeCoupon(String code) {
        return code == null || code.isBlank() ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    private Optional<User> findActiveUser(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .filter(u -> u.getUserStatus() != null && u.getUserStatus() == Constants.ACTIVE_USER_STATUS);
    }

    /** Users table is shared with the appointments app: an active user may not have a store profile yet. */
    private StoreProfile resolveProfile(User user) {
        return storeProfileDao.findByUserId(user.getId()).orElseGet(() -> {
            StoreProfile profile = new StoreProfile();
            profile.setUser(user);
            profile.getRoles().add(StoreRole.CUSTOMER);
            return storeProfileDao.save(profile);
        });
    }

    private void requireStoreManager(Long storeId, Long userId) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .filter(m -> m.getRole() != StoreMemberRole.STAFF)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED,
                        "Only the store's OWNER or MANAGER can register orders for customers"));
    }

    private static String shortName(User user) {
        String name = user.getName() == null ? "" : user.getName().trim();
        String lastname = user.getLastname() == null ? "" : user.getLastname().trim();
        return lastname.isEmpty() ? name : name + " " + lastname.charAt(0) + ".";
    }
}
