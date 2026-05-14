package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.cartModule.domain.model.Cart;
import com.forehapp.store.cartModule.domain.model.CartItem;
import com.forehapp.store.cartModule.domain.model.CartStatus;
import com.forehapp.store.cartModule.domain.ports.out.ICartDao;
import com.forehapp.store.orderModule.application.mappers.OrderMapper;
import com.forehapp.store.orderModule.domain.events.LowStockEvent;
import com.forehapp.store.orderModule.domain.events.OrderCreatedEvent;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.ports.in.IOrderService;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.orderModule.infrastructure.web.dto.CreateOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderSummaryDto;
import com.forehapp.store.paymentModule.domain.ports.in.IPaymentService;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.UserAddress;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.IUserAddressRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    private final ICartDao cartDao;
    private final IOrderDao orderDao;
    private final IStoreProfileDao storeProfileDao;
    private final IUserAddressRepository addressRepository;
    private final IProductVariantDao productVariantDao;
    private final IPaymentService paymentService;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    public OrderServiceImpl(ICartDao cartDao,
                            IOrderDao orderDao,
                            IStoreProfileDao storeProfileDao,
                            IUserAddressRepository addressRepository,
                            IProductVariantDao productVariantDao,
                            IPaymentService paymentService,
                            OrderMapper orderMapper,
                            ApplicationEventPublisher eventPublisher) {
        this.cartDao = cartDao;
        this.orderDao = orderDao;
        this.storeProfileDao = storeProfileDao;
        this.addressRepository = addressRepository;
        this.productVariantDao = productVariantDao;
        this.paymentService = paymentService;
        this.orderMapper = orderMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequestDto dto) {
        StoreProfile buyer = resolveProfile(userId);

        Cart cart = cartDao.findActiveByBuyerId(buyer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cart is empty");
        }

        UserAddress address = addressRepository.findById(dto.addressId())
                .filter(a -> a.getStoreProfile().getId().equals(buyer.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        preValidateStock(cart.getItems());

        Order order = buildOrder(buyer, address, cart.getItems());
        Order savedOrder = orderDao.save(order);

        cart.setStatus(CartStatus.CONVERTED);
        cart.setUpdatedAt(LocalDateTime.now());
        cartDao.save(cart);

        String checkoutUrl = paymentService.createMercadoPagoPreference(savedOrder);

        eventPublisher.publishEvent(buildOrderCreatedEvent(savedOrder, buyer));

        return orderMapper.toResponse(savedOrder, checkoutUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        StoreProfile buyer = resolveProfile(userId);
        Order order = orderDao.findById(orderId)
                .filter(o -> o.getBuyer().getId().equals(buyer.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return orderMapper.toResponse(order, null);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private void preValidateStock(List<CartItem> items) {
        for (CartItem item : items) {
            ProductVariant variant = item.getVariant();
            if (variant.getStock() < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Insufficient stock for: " + variant.getProduct().getTitle() + " (SKU: " + variant.getSku() + ")");
            }
        }
    }

    private Order buildOrder(StoreProfile buyer, UserAddress address, List<CartItem> cartItems) {
        Order order = new Order();
        order.setBuyer(buyer);
        order.setShippingAddress(address.getStreet());
        order.setShippingCity(address.getCity());
        order.setShippingCountry(address.getCountry());

        Map<Long, List<CartItem>> bySeller = cartItems.stream()
                .collect(Collectors.groupingBy(i -> i.getVariant().getProduct().getSeller().getId()));

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, List<CartItem>> entry : bySeller.entrySet()) {
            StoreProfile seller = entry.getValue().get(0).getVariant().getProduct().getSeller();
            OrderSellerGroup group = buildSellerGroup(order, seller, entry.getValue());
            order.getSellerGroups().add(group);
            total = total.add(group.getSubtotal());
        }

        order.setTotal(total);
        return order;
    }

    private OrderSellerGroup buildSellerGroup(Order order, StoreProfile seller, List<CartItem> items) {
        OrderSellerGroup group = new OrderSellerGroup();
        group.setOrder(order);
        group.setSeller(seller);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : items) {
            ProductVariant variant = productVariantDao.findByIdForUpdate(cartItem.getVariant().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variant not found"));

            if (variant.getStock() < cartItem.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
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

            if (newStock <= lowStockThreshold) {
                String sellerEmail = group.getSeller().getUser().getEmail();
                String sellerName = group.getSeller().getUser().getName() + " " + group.getSeller().getUser().getLastname();
                eventPublisher.publishEvent(new LowStockEvent(sellerEmail, sellerName,
                        variant.getProduct().getTitle(), variant.getSku(), newStock));
            }
        }

        group.setSubtotal(subtotal);
        return group;
    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order, StoreProfile buyer) {
        String buyerName = buyer.getUser().getName() + " " + buyer.getUser().getLastname();

        List<OrderCreatedEvent.SellerGroupData> sellerGroups = order.getSellerGroups().stream()
                .map(group -> {
                    String sellerEmail = group.getSeller().getUser().getEmail();
                    String sellerName = group.getSeller().getUser().getName() + " " + group.getSeller().getUser().getLastname();

                    List<OrderCreatedEvent.ItemData> items = group.getItems().stream()
                            .map(item -> new OrderCreatedEvent.ItemData(
                                    item.getVariant().getProduct().getTitle(),
                                    item.getVariant().getSku(),
                                    item.getQuantity(),
                                    item.getUnitPrice(),
                                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                            ))
                            .toList();

                    return new OrderCreatedEvent.SellerGroupData(sellerEmail, sellerName, group.getSubtotal(), items);
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

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryDto> getMyOrders(Long userId) {
        StoreProfile buyer = resolveProfile(userId);
        return orderDao.findAllByBuyerIdOrderByCreatedAtDesc(buyer.getId()).stream()
                .map(orderMapper::toSummary)
                .toList();
    }

    private StoreProfile resolveProfile(Long userId) {
        return storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
    }
}
