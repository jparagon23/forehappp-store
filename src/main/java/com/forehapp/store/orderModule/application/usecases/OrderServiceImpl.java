package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.cartModule.domain.model.Cart;
import com.forehapp.store.cartModule.domain.model.CartItem;
import com.forehapp.store.cartModule.domain.model.CartStatus;
import com.forehapp.store.cartModule.domain.ports.out.ICartDao;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.orderModule.application.mappers.OrderMapper;
import com.forehapp.store.orderModule.domain.events.LowStockEvent;
import com.forehapp.store.orderModule.domain.events.OrderCreatedEvent;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.domain.ports.in.IOrderService;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.orderModule.infrastructure.web.dto.CreateOrderRequestDto;
import com.forehapp.store.ambassadorModule.domain.model.Ambassador;
import com.forehapp.store.ambassadorModule.domain.model.AmbassadorCommission;
import com.forehapp.store.ambassadorModule.domain.model.AmbassadorStatus;
import com.forehapp.store.ambassadorModule.domain.ports.out.IAmbassadorDao;
import com.forehapp.store.ambassadorModule.domain.ports.out.ICommissionDao;
import com.forehapp.store.donationModule.domain.model.DonationFoundation;
import com.forehapp.store.donationModule.domain.model.DonationRecord;
import com.forehapp.store.donationModule.domain.ports.out.IDonationFoundationDao;
import com.forehapp.store.donationModule.domain.ports.out.IDonationRecordDao;
import com.forehapp.store.promotionModule.application.dto.CouponValidationResponse;
import com.forehapp.store.promotionModule.application.dto.RedeemCouponRequestDto;
import com.forehapp.store.promotionModule.domain.ports.in.IPromotionService;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderSummaryDto;
import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import com.forehapp.store.paymentModule.domain.ports.in.IPaymentService;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductStatus;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import com.forehapp.store.shippingModule.domain.ports.out.IShippingZoneDao;
import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.model.UserAddress;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.IUserAddressRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    private final ICartDao cartDao;
    private final IOrderDao orderDao;
    private final IStoreProfileDao storeProfileDao;
    private final IStoreMembershipDao membershipDao;
    private final IUserAddressRepository addressRepository;
    private final IProductVariantDao productVariantDao;
    private final IProductDao productDao;
    private final IPaymentService paymentService;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final IShippingZoneDao shippingZoneDao;
    private final IPromotionService promotionService;
    private final IAmbassadorDao ambassadorDao;
    private final ICommissionDao commissionDao;
    private final IDonationFoundationDao donationFoundationDao;
    private final IDonationRecordDao donationRecordDao;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    @Value("${app.payment.mercado-pago-surcharge-rate:0.03}")
    private BigDecimal mercadoPagoSurchargeRate;

    public OrderServiceImpl(ICartDao cartDao,
                            IOrderDao orderDao,
                            IStoreProfileDao storeProfileDao,
                            IStoreMembershipDao membershipDao,
                            IUserAddressRepository addressRepository,
                            IProductVariantDao productVariantDao,
                            IProductDao productDao,
                            IPaymentService paymentService,
                            OrderMapper orderMapper,
                            ApplicationEventPublisher eventPublisher,
                            IShippingZoneDao shippingZoneDao,
                            IPromotionService promotionService,
                            IAmbassadorDao ambassadorDao,
                            ICommissionDao commissionDao,
                            IDonationFoundationDao donationFoundationDao,
                            IDonationRecordDao donationRecordDao) {
        this.cartDao = cartDao;
        this.orderDao = orderDao;
        this.storeProfileDao = storeProfileDao;
        this.membershipDao = membershipDao;
        this.addressRepository = addressRepository;
        this.productVariantDao = productVariantDao;
        this.productDao = productDao;
        this.paymentService = paymentService;
        this.orderMapper = orderMapper;
        this.eventPublisher = eventPublisher;
        this.shippingZoneDao = shippingZoneDao;
        this.promotionService = promotionService;
        this.ambassadorDao = ambassadorDao;
        this.commissionDao = commissionDao;
        this.donationFoundationDao = donationFoundationDao;
        this.donationRecordDao = donationRecordDao;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"public-products", "discovery-sections"}, allEntries = true)
    public OrderResponse createOrder(Long userId, CreateOrderRequestDto dto) {
        StoreProfile buyer = resolveProfile(userId);

        if (buyer.getPhone() == null || buyer.getPhone().isBlank()) {
            throw new BadRequestException(ErrorCode.ORDER_BUYER_PHONE_REQUIRED,
                    "A phone number is required to place an order. Please update your profile.");
        }

        Cart cart = cartDao.findActiveByBuyerId(buyer.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_CART_NOT_FOUND, "Active cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException(ErrorCode.ORDER_CART_EMPTY, "Cart is empty");
        }

        UserAddress address = addressRepository.findById(dto.addressId())
                .filter(a -> a.getStoreProfile().getId().equals(buyer.getId()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_ADDRESS_NOT_FOUND, "Address not found"));

        if (dto.paymentMethod() == PaymentMethod.CASH_ON_DELIVERY
                && !address.getCity().getName().equalsIgnoreCase("Cali")) {
            throw new BadRequestException(ErrorCode.ORDER_ADDRESS_STORE_MISMATCH,
                    "Cash on delivery is only available for orders shipped to Cali");
        }

        preValidateStock(cart.getItems());

        Order order = buildOrder(buyer, address, cart.getItems());
        order.setPaymentMethod(dto.paymentMethod().name());
        Order savedOrder = orderDao.save(order);

        if (dto.couponCode() != null) {
            savedOrder = applyCoupon(userId, dto.couponCode(), dto.couponStoreId(), savedOrder, dto.referralCode());
        }

        if (dto.referralCode() != null && !dto.referralCode().isBlank()) {
            applyReferralCode(dto.referralCode().toUpperCase(), savedOrder);
        }

        if (dto.paymentMethod() == PaymentMethod.MERCADO_PAGO) {
            BigDecimal surcharge = savedOrder.getTotal()
                    .multiply(mercadoPagoSurchargeRate)
                    .setScale(2, RoundingMode.HALF_UP);
            savedOrder.setMercadoPagoSurcharge(surcharge);
            savedOrder.setTotal(savedOrder.getTotal().add(surcharge));
            savedOrder = orderDao.save(savedOrder);
        }

        cart.setStatus(CartStatus.CONVERTED);
        cart.setUpdatedAt(LocalDateTime.now());
        cartDao.save(cart);

        String checkoutUrl = switch (dto.paymentMethod()) {
            case MERCADO_PAGO -> paymentService.createMercadoPagoPreference(savedOrder);
            case CASH, TRANSFER -> {
                paymentService.createPendingPayment(savedOrder, dto.paymentMethod());
                yield null;
            }
            case CASH_ON_DELIVERY -> {
                paymentService.createPendingPayment(savedOrder, PaymentMethod.CASH_ON_DELIVERY);
                transitionGroupsToPreparing(savedOrder);
                yield null;
            }
        };

        eventPublisher.publishEvent(buildOrderCreatedEvent(savedOrder, buyer));

        return orderMapper.toResponse(savedOrder, checkoutUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        StoreProfile buyer = resolveProfile(userId);
        Order order = orderDao.findById(orderId)
                .filter(o -> o.getBuyer() != null && o.getBuyer().getId().equals(buyer.getId()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order not found"));
        return orderMapper.toResponse(order, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryDto> getMyOrders(Long userId) {
        StoreProfile buyer = resolveProfile(userId);
        return orderDao.findAllByBuyerIdOrderByCreatedAtDesc(buyer.getId()).stream()
                .map(orderMapper::toSummary)
                .toList();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private void transitionGroupsToPreparing(Order order) {
        LocalDateTime now = LocalDateTime.now();
        order.getSellerGroups().forEach(g -> {
            g.setStatus(OrderSellerGroupStatus.PREPARING);
            g.setPreparedAt(now);
        });
        orderDao.save(order);
    }

    private void preValidateStock(List<CartItem> items) {
        for (CartItem item : items) {
            ProductVariant variant = item.getVariant();
            if (variant.getStock() < item.getQuantity()) {
                throw new ConflictException(ErrorCode.ORDER_INSUFFICIENT_STOCK,
                        "Insufficient stock for: " + variant.getProduct().getTitle() + " (SKU: " + variant.getSku() + ")");
            }
        }
    }

    private Order buildOrder(StoreProfile buyer, UserAddress address, List<CartItem> cartItems) {
        Order order = new Order();
        order.setBuyer(buyer);
        order.setBuyerPhone(buyer.getPhone());
        order.setBuyerEmail(buyer.getUser().getEmail());
        order.setShippingAddress(address.getStreet());
        order.setShippingCity(address.getCity().getName());
        order.setShippingCountry(address.getCity().getState().getCountry().getName());

        Map<Long, List<CartItem>> byStore = cartItems.stream()
                .collect(Collectors.groupingBy(i -> i.getVariant().getProduct().getStore().getId()));

        BigDecimal total = BigDecimal.ZERO;

        Long cityId = address.getCity().getId();
        for (Map.Entry<Long, List<CartItem>> entry : byStore.entrySet()) {
            Store store = entry.getValue().get(0).getVariant().getProduct().getStore();
            OrderSellerGroup group = buildStoreGroup(order, store, entry.getValue(), cityId);
            order.getSellerGroups().add(group);
            total = total.add(group.getSubtotal()).add(group.getShippingCost());
        }

        order.setTotal(total);
        return order;
    }

    private OrderSellerGroup buildStoreGroup(Order order, Store store, List<CartItem> items, Long cityId) {
        OrderSellerGroup group = new OrderSellerGroup();
        group.setOrder(order);
        group.setStore(store);

        BigDecimal subtotal = BigDecimal.ZERO;
        Set<Long> affectedProductIds = new HashSet<>();

        for (CartItem cartItem : items) {
            ProductVariant variant = productVariantDao.findByIdForUpdate(cartItem.getVariant().getId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_VARIANT_NOT_FOUND, "Variant not found"));

            if (variant.getStock() < cartItem.getQuantity()) {
                throw new ConflictException(ErrorCode.ORDER_INSUFFICIENT_STOCK,
                        "Insufficient stock for: " + variant.getProduct().getTitle());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setSellerGroup(group);
            orderItem.setVariant(variant);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(variant.getPrice());
            orderItem.setUnitCost(variant.getCost());
            group.getItems().add(orderItem);

            subtotal = subtotal.add(variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            int newStock = variant.getStock() - cartItem.getQuantity();
            variant.setStock(newStock);
            productVariantDao.save(variant);

            affectedProductIds.add(variant.getProduct().getId());

            if (newStock <= lowStockThreshold) {
                membershipDao.findActiveByStoreId(store.getId()).stream()
                        .filter(m -> m.getRole() == StoreMemberRole.OWNER)
                        .findFirst()
                        .ifPresent(ownerMembership -> {
                            String ownerEmail = ownerMembership.getStoreProfile().getUser().getEmail();
                            String ownerName = ownerMembership.getStoreProfile().getUser().getName()
                                    + " " + ownerMembership.getStoreProfile().getUser().getLastname();
                            eventPublisher.publishEvent(new LowStockEvent(ownerEmail, ownerName,
                                    variant.getProduct().getTitle(), variant.getSku(), newStock));
                        });
            }
        }

        for (Long productId : affectedProductIds) {
            Product product = productDao.findById(productId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_VARIANT_NOT_FOUND, "Product not found"));
            if (product.getStatus() == ProductStatus.ACTIVE) {
                boolean allOutOfStock = product.getVariants().stream().allMatch(v -> v.getStock() <= 0);
                if (allOutOfStock) {
                    product.setStatus(ProductStatus.OUT_OF_STOCK);
                    productDao.save(product);
                }
            }
        }

        group.setSubtotal(subtotal);
        group.setShippingCost(resolveShippingCost(cityId, store, subtotal, items));
        return group;
    }

    private BigDecimal resolveShippingCost(Long cityId, Store store, BigDecimal subtotal, List<CartItem> items) {
        if (store.getFreeShippingMinAmount() != null
                && subtotal.compareTo(store.getFreeShippingMinAmount()) >= 0) {
            return BigDecimal.ZERO;
        }
        boolean allItemsFreeShipping = items.stream()
                .allMatch(i -> Boolean.TRUE.equals(i.getVariant().getProduct().getFreeShipping()));
        if (allItemsFreeShipping) {
            return BigDecimal.ZERO;
        }
        return shippingZoneDao.findActiveByCityId(cityId)
                .or(shippingZoneDao::findActiveDefault)
                .map(com.forehapp.store.shippingModule.domain.model.ShippingZone::getCost)
                .orElse(BigDecimal.ZERO);
    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order, StoreProfile buyer) {
        String buyerName = buyer.getUser().getName() + " " + buyer.getUser().getLastname();

        List<OrderCreatedEvent.SellerGroupData> sellerGroups = order.getSellerGroups().stream()
                .map(group -> {
                    Store store = group.getStore();
                    List<String> memberEmails = membershipDao.findActiveByStoreId(store.getId()).stream()
                            .map(m -> m.getStoreProfile().getUser().getEmail())
                            .toList();

                    List<OrderCreatedEvent.ItemData> items = group.getItems().stream()
                            .map(item -> new OrderCreatedEvent.ItemData(
                                    item.getVariant().getProduct().getTitle(),
                                    item.getVariant().getSku(),
                                    item.getQuantity(),
                                    item.getUnitPrice(),
                                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                            ))
                            .toList();

                    return new OrderCreatedEvent.SellerGroupData(memberEmails, store.getName(), group.getSubtotal(), group.getShippingCost(), items);
                })
                .toList();

        return new OrderCreatedEvent(
                order.getId(),
                buyerName,
                order.getBuyerEmail(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingCountry(),
                order.getCreatedAt(),
                order.getTotal(),
                order.getPaymentMethod(),
                sellerGroups
        );
    }

    private Order applyCoupon(Long userId, String couponCode, Long couponStoreId, Order savedOrder, String referralCode) {
        BigDecimal applicableAmount;
        BigDecimal applicableShipping = BigDecimal.ZERO;

        if (couponStoreId != null) {
            boolean hasGroupForStore = savedOrder.getSellerGroups().stream()
                    .anyMatch(g -> g.getStore().getId().equals(couponStoreId));
            if (!hasGroupForStore) {
                throw new BadRequestException(ErrorCode.COUPON_INVALID,
                        "Cart has no items from the coupon's store");
            }
            OrderSellerGroup group = savedOrder.getSellerGroups().stream()
                    .filter(g -> g.getStore().getId().equals(couponStoreId))
                    .findFirst()
                    .orElse(null);
            applicableAmount = group != null ? group.getSubtotal() : BigDecimal.ZERO;
            applicableShipping = group != null ? group.getShippingCost() : BigDecimal.ZERO;
        } else {
            applicableAmount = savedOrder.getTotal();
        }

        CouponValidationResponse couponResult = promotionService.redeemCoupon(userId,
                new RedeemCouponRequestDto(couponCode, couponStoreId, applicableAmount, savedOrder.getId(), applicableShipping));

        if (couponResult.isDonation()) {
            if (referralCode != null && !referralCode.isBlank()) {
                throw new BadRequestException(ErrorCode.DONATION_COUPON_INCOMPATIBLE_WITH_REFERRAL,
                        "Donation coupons cannot be combined with ambassador referral codes");
            }
            savedOrder.setCouponCode(couponCode);
            savedOrder.setCouponDiscount(BigDecimal.ZERO);

            DonationFoundation foundation = donationFoundationDao.findById(couponResult.foundationId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.DONATION_FOUNDATION_NOT_FOUND, "Foundation not found"));
            DonationRecord record = new DonationRecord();
            record.setFoundation(foundation);
            record.setOrderId(savedOrder.getId());
            record.setCouponCode(couponCode);
            record.setDonorProfile(savedOrder.getBuyer());
            record.setDonorEmail(savedOrder.getBuyerEmail());
            record.setDonationAmount(couponResult.donationAmount());
            record.setDonationPercentage(couponResult.discountValue());
            donationRecordDao.save(record);
        } else {
            savedOrder.setCouponCode(couponCode);
            savedOrder.setCouponDiscount(couponResult.discountAmount());
            savedOrder.setTotal(savedOrder.getTotal().subtract(couponResult.discountAmount()).max(BigDecimal.ZERO));
        }
        return orderDao.save(savedOrder);
    }

    private void applyReferralCode(String referralCode, Order order) {
        ambassadorDao.findByReferralCode(referralCode)
                .filter(a -> a.getStatus() == AmbassadorStatus.ACTIVE)
                .ifPresent(ambassador -> {
                    order.setReferralCode(referralCode);
                    orderDao.save(order);

                    java.math.BigDecimal commissionAmount = order.getTotal()
                            .multiply(ambassador.getCommissionPercentage())
                            .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

                    AmbassadorCommission commission = new AmbassadorCommission();
                    commission.setAmbassador(ambassador);
                    commission.setOrderId(order.getId());
                    commission.setCommissionAmount(commissionAmount);
                    commission.setCommissionPercentage(ambassador.getCommissionPercentage());
                    commissionDao.save(commission);
                });
    }

    private StoreProfile resolveProfile(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.CUSTOMER)) {
            throw new ForbiddenException(ErrorCode.ORDER_CUSTOMER_ROLE_REQUIRED, "User does not have CUSTOMER role");
        }
        return profile;
    }
}
