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

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

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
                            IShippingZoneDao shippingZoneDao) {
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
    }

    @Override
    @Transactional
    @CacheEvict(value = "public-products", allEntries = true)
    public OrderResponse createOrder(Long userId, CreateOrderRequestDto dto) {
        StoreProfile buyer = resolveProfile(userId);

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
                .filter(o -> o.getBuyer().getId().equals(buyer.getId()))
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
                    String storeEmail = membershipDao.findActiveByStoreId(store.getId()).stream()
                            .filter(m -> m.getRole() == StoreMemberRole.OWNER)
                            .findFirst()
                            .map(m -> m.getStoreProfile().getUser().getEmail())
                            .orElse(null);

                    List<OrderCreatedEvent.ItemData> items = group.getItems().stream()
                            .map(item -> new OrderCreatedEvent.ItemData(
                                    item.getVariant().getProduct().getTitle(),
                                    item.getVariant().getSku(),
                                    item.getQuantity(),
                                    item.getUnitPrice(),
                                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                            ))
                            .toList();

                    return new OrderCreatedEvent.SellerGroupData(storeEmail, store.getName(), group.getSubtotal(), items);
                })
                .toList();

        return new OrderCreatedEvent(
                order.getId(),
                buyerName,
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingCountry(),
                order.getCreatedAt(),
                sellerGroups
        );
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
