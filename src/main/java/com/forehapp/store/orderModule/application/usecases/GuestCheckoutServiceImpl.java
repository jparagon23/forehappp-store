package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.cartModule.application.dto.ShippingEstimateGroupResponse;
import com.forehapp.store.cartModule.application.dto.ShippingEstimateResponse;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.locationModule.domain.model.City;
import com.forehapp.store.locationModule.domain.ports.out.ICityDao;
import com.forehapp.store.orderModule.application.mappers.OrderMapper;
import com.forehapp.store.orderModule.domain.events.LowStockEvent;
import com.forehapp.store.orderModule.domain.events.OrderCreatedEvent;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.domain.ports.in.IGuestCheckoutService;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestCreateOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestOrderItemDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestShippingEstimateRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class GuestCheckoutServiceImpl implements IGuestCheckoutService {

    private final IOrderDao orderDao;
    private final IStoreMembershipDao membershipDao;
    private final IProductVariantDao productVariantDao;
    private final IProductDao productDao;
    private final IPaymentService paymentService;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final IShippingZoneDao shippingZoneDao;
    private final ICityDao cityDao;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    public GuestCheckoutServiceImpl(IOrderDao orderDao,
                                    IStoreMembershipDao membershipDao,
                                    IProductVariantDao productVariantDao,
                                    IProductDao productDao,
                                    IPaymentService paymentService,
                                    OrderMapper orderMapper,
                                    ApplicationEventPublisher eventPublisher,
                                    IShippingZoneDao shippingZoneDao,
                                    ICityDao cityDao) {
        this.orderDao = orderDao;
        this.membershipDao = membershipDao;
        this.productVariantDao = productVariantDao;
        this.productDao = productDao;
        this.paymentService = paymentService;
        this.orderMapper = orderMapper;
        this.eventPublisher = eventPublisher;
        this.shippingZoneDao = shippingZoneDao;
        this.cityDao = cityDao;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"public-products", "discovery-sections"}, allEntries = true)
    public OrderResponse placeOrder(GuestCreateOrderRequestDto dto) {
        City city = cityDao.findById(dto.shippingCityId())
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_ADDRESS_NOT_FOUND, "City not found"));

        if (dto.paymentMethod() == PaymentMethod.CASH_ON_DELIVERY
                && !city.getName().equalsIgnoreCase("Cali")) {
            throw new BadRequestException(ErrorCode.ORDER_ADDRESS_STORE_MISMATCH,
                    "Cash on delivery is only available for orders shipped to Cali");
        }

        Map<Long, List<GuestOrderItemDto>> itemsByStore = groupItemsByStore(dto.items());

        Order order = buildGuestOrder(dto, city, itemsByStore);
        Order savedOrder = orderDao.save(order);

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

        eventPublisher.publishEvent(buildOrderCreatedEvent(savedOrder));

        return orderMapper.toResponse(savedOrder, checkoutUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingEstimateResponse estimateShipping(GuestShippingEstimateRequestDto dto) {
        City city = cityDao.findById(dto.cityId())
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_ADDRESS_NOT_FOUND, "City not found"));

        Map<Long, List<GuestOrderItemDto>> itemsByStore = groupItemsByStore(dto.items());

        List<ShippingEstimateGroupResponse> groups = itemsByStore.entrySet().stream()
                .map(entry -> {
                    List<GuestOrderItemDto> storeItems = entry.getValue();
                    ProductVariant first = productVariantDao.findById(storeItems.get(0).variantId()).orElseThrow();
                    Store store = first.getProduct().getStore();

                    List<ProductVariant> variants = storeItems.stream()
                            .map(i -> productVariantDao.findById(i.variantId()).orElseThrow())
                            .toList();

                    BigDecimal subtotal = IntStream.range(0, storeItems.size())
                            .mapToObj(i -> variants.get(i).getPrice()
                                    .multiply(BigDecimal.valueOf(storeItems.get(i).quantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal shippingCost = resolveShippingCost(city.getId(), store, subtotal, variants);
                    return new ShippingEstimateGroupResponse(
                            store.getId(),
                            store.getName(),
                            subtotal,
                            shippingCost,
                            shippingCost.compareTo(BigDecimal.ZERO) == 0
                    );
                })
                .toList();

        BigDecimal itemsTotal = groups.stream()
                .map(ShippingEstimateGroupResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingTotal = groups.stream()
                .map(ShippingEstimateGroupResponse::shippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ShippingEstimateResponse(groups, itemsTotal, shippingTotal, itemsTotal.add(shippingTotal));
    }

    private Order buildGuestOrder(GuestCreateOrderRequestDto dto, City city,
                                   Map<Long, List<GuestOrderItemDto>> itemsByStore) {
        Order order = new Order();
        order.setGuestName(dto.name());
        order.setGuestLastname(dto.lastname());
        order.setBuyerEmail(dto.email());
        order.setBuyerPhone(dto.phone());
        order.setShippingAddress(dto.shippingAddress());
        order.setShippingCity(city.getName());
        order.setShippingDepartment(city.getState().getName());
        order.setShippingCountry(city.getState().getCountry().getName());
        order.setShippingComplement(dto.shippingComplement());
        order.setShippingReference(dto.shippingReference());
        order.setShippingCityId(city.getId());
        order.setPaymentMethod(dto.paymentMethod().name());

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, List<GuestOrderItemDto>> entry : itemsByStore.entrySet()) {
            OrderSellerGroup group = buildStoreGroup(order, entry.getValue(), city.getId());
            order.getSellerGroups().add(group);
            total = total.add(group.getSubtotal()).add(group.getShippingCost());
        }

        order.setTotal(total);
        return order;
    }

    private Map<Long, List<GuestOrderItemDto>> groupItemsByStore(List<GuestOrderItemDto> items) {
        Map<Long, List<GuestOrderItemDto>> byStore = new LinkedHashMap<>();
        for (GuestOrderItemDto item : items) {
            ProductVariant variant = productVariantDao.findById(item.variantId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_VARIANT_NOT_FOUND,
                            "Variant not found: " + item.variantId()));
            if (!Boolean.TRUE.equals(variant.getActive())) {
                throw new BadRequestException(ErrorCode.ORDER_VARIANT_NOT_FOUND,
                        "Variant is no longer available: " + variant.getSku());
            }
            Long storeId = variant.getProduct().getStore().getId();
            byStore.computeIfAbsent(storeId, k -> new ArrayList<>()).add(item);
        }
        return byStore;
    }

    private OrderSellerGroup buildStoreGroup(Order order, List<GuestOrderItemDto> items, Long cityId) {
        OrderSellerGroup group = new OrderSellerGroup();
        group.setOrder(order);

        BigDecimal subtotal = BigDecimal.ZERO;
        Set<Long> affectedProductIds = new HashSet<>();
        List<ProductVariant> loadedVariants = new ArrayList<>();
        Store store = null;

        for (GuestOrderItemDto item : items) {
            ProductVariant variant = productVariantDao.findByIdForUpdate(item.variantId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_VARIANT_NOT_FOUND,
                            "Variant not found: " + item.variantId()));

            if (variant.getStock() < item.quantity()) {
                throw new ConflictException(ErrorCode.ORDER_INSUFFICIENT_STOCK,
                        "Insufficient stock for: " + variant.getProduct().getTitle());
            }

            if (store == null) {
                store = variant.getProduct().getStore();
                group.setStore(store);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setSellerGroup(group);
            orderItem.setVariant(variant);
            orderItem.setQuantity(item.quantity());
            orderItem.setUnitPrice(variant.getPrice());
            group.getItems().add(orderItem);
            loadedVariants.add(variant);

            subtotal = subtotal.add(variant.getPrice().multiply(BigDecimal.valueOf(item.quantity())));

            int newStock = variant.getStock() - item.quantity();
            variant.setStock(newStock);
            productVariantDao.save(variant);

            affectedProductIds.add(variant.getProduct().getId());

            if (newStock <= lowStockThreshold) {
                notifyLowStock(store, variant, newStock);
            }
        }

        for (Long productId : affectedProductIds) {
            markOutOfStockIfNeeded(productId);
        }

        group.setSubtotal(subtotal);
        group.setShippingCost(resolveShippingCost(cityId, store, subtotal, loadedVariants));
        return group;
    }

    private BigDecimal resolveShippingCost(Long cityId, Store store, BigDecimal subtotal,
                                            List<ProductVariant> loadedVariants) {
        if (store.getFreeShippingMinAmount() != null
                && subtotal.compareTo(store.getFreeShippingMinAmount()) >= 0) {
            return BigDecimal.ZERO;
        }
        boolean allItemsFreeShipping = loadedVariants.stream()
                .allMatch(v -> Boolean.TRUE.equals(v.getProduct().getFreeShipping()));
        if (allItemsFreeShipping) {
            return BigDecimal.ZERO;
        }
        return shippingZoneDao.findActiveByCityId(cityId)
                .or(shippingZoneDao::findActiveDefault)
                .map(com.forehapp.store.shippingModule.domain.model.ShippingZone::getCost)
                .orElse(BigDecimal.ZERO);
    }

    private void transitionGroupsToPreparing(Order order) {
        LocalDateTime now = LocalDateTime.now();
        order.getSellerGroups().forEach(g -> {
            g.setStatus(OrderSellerGroupStatus.PREPARING);
            g.setPreparedAt(now);
        });
        orderDao.save(order);
    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order) {
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
                    return new OrderCreatedEvent.SellerGroupData(memberEmails, store.getName(),
                            group.getSubtotal(), items);
                })
                .toList();

        return new OrderCreatedEvent(
                order.getId(),
                order.resolveContactName(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingCountry(),
                order.getCreatedAt(),
                sellerGroups
        );
    }

    private void notifyLowStock(Store store, ProductVariant variant, int newStock) {
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

    private void markOutOfStockIfNeeded(Long productId) {
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
}
