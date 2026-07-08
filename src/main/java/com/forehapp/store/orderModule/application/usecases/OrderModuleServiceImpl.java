package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.orderModule.domain.events.OrderStatusChangedEvent;
import com.forehapp.store.orderModule.domain.events.ShippingCostRemovedEvent;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.domain.model.OrderStatus;
import com.forehapp.store.orderModule.domain.ports.in.IOrderModuleService;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.orderModule.domain.ports.out.IOrderGroupDao;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderItemDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.SellerOrderGroupDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.VariantAttributeDto;
import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import com.forehapp.store.paymentModule.domain.model.PaymentStatus;
import com.forehapp.store.paymentModule.infrastructure.persistence.IPaymentRepository;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderModuleServiceImpl implements IOrderModuleService {

    private final IOrderGroupDao orderGroupDao;
    private final IOrderDao orderDao;
    private final IPaymentRepository paymentRepository;
    private final IStoreMembershipDao membershipDao;
    private final ApplicationEventPublisher eventPublisher;

    public OrderModuleServiceImpl(IOrderGroupDao orderGroupDao,
                                  IOrderDao orderDao,
                                  IPaymentRepository paymentRepository,
                                  IStoreMembershipDao membershipDao,
                                  ApplicationEventPublisher eventPublisher) {
        this.orderGroupDao = orderGroupDao;
        this.orderDao = orderDao;
        this.paymentRepository = paymentRepository;
        this.membershipDao = membershipDao;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerOrderGroupDto> getSellerGroups(Long storeId, Long userId) {
        resolveStoreAccess(storeId, userId);
        return orderGroupDao.findAllByStoreIdWithDetails(storeId).stream()
                .map(this::toSellerDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SellerOrderGroupDto getSellerGroupById(Long storeId, Long groupId, Long userId) {
        resolveStoreAccess(storeId, userId);
        OrderSellerGroup group = resolveGroup(groupId, storeId);
        return toSellerDto(group);
    }

    @Override
    @Transactional
    public void prepareGroup(Long storeId, Long groupId, Long userId) {
        resolveStoreAccess(storeId, userId);
        OrderSellerGroup group = resolveGroup(groupId, storeId);

        if (group.getStatus() != OrderSellerGroupStatus.PENDING) {
            throw new ConflictException(ErrorCode.ORDER_GROUP_INVALID_STATUS,
                    "Group must be in PENDING status to start preparing");
        }

        group.setStatus(OrderSellerGroupStatus.PREPARING);
        group.setPreparedAt(LocalDateTime.now());
        orderGroupDao.save(group);

        eventPublisher.publishEvent(buildStatusEvent(group));
    }

    @Override
    @Transactional
    public void shipGroup(Long storeId, Long groupId, String trackingNumber, Long userId) {
        resolveStoreAccess(storeId, userId);
        OrderSellerGroup group = resolveGroup(groupId, storeId);

        if (group.getStatus() != OrderSellerGroupStatus.PREPARING) {
            throw new ConflictException(ErrorCode.ORDER_GROUP_INVALID_STATUS,
                    "Group must be in PREPARING status to ship");
        }

        group.setStatus(OrderSellerGroupStatus.SHIPPED);
        group.setTrackingNumber(trackingNumber);
        group.setShippedAt(LocalDateTime.now());
        orderGroupDao.save(group);

        eventPublisher.publishEvent(buildStatusEvent(group));
    }

    @Override
    @Transactional
    public void deliverGroup(Long storeId, Long groupId, Long userId) {
        resolveStoreAccess(storeId, userId);
        OrderSellerGroup group = resolveGroup(groupId, storeId);

        if (group.getStatus() != OrderSellerGroupStatus.SHIPPED) {
            throw new ConflictException(ErrorCode.ORDER_GROUP_INVALID_STATUS,
                    "Group must be in SHIPPED status to confirm delivery");
        }

        group.setStatus(OrderSellerGroupStatus.DELIVERED);
        group.setDeliveredAt(LocalDateTime.now());
        orderGroupDao.save(group);

        eventPublisher.publishEvent(buildStatusEvent(group));

        Long orderId = group.getOrder().getId();
        orderDao.findBasicById(orderId).ifPresent(order -> {
            if (PaymentMethod.CASH_ON_DELIVERY.name().equals(order.getPaymentMethod())) {
                boolean allDelivered = orderGroupDao.findAllByOrderId(orderId).stream()
                        .allMatch(g -> g.getStatus() == OrderSellerGroupStatus.DELIVERED);
                if (allDelivered) {
                    order.setStatus(OrderStatus.PAID);
                    orderDao.save(order);
                    paymentRepository.findByOrderId(orderId).ifPresent(p -> {
                        p.setStatus(PaymentStatus.APPROVED.name());
                        paymentRepository.save(p);
                    });
                }
            }
        });
    }

    @Override
    @Transactional
    public void cancelGroup(Long storeId, Long groupId, String reason, Long userId) {
        resolveStoreAccess(storeId, userId);
        OrderSellerGroup group = resolveGroup(groupId, storeId);

        if (group.getStatus() == OrderSellerGroupStatus.SHIPPED
                || group.getStatus() == OrderSellerGroupStatus.DELIVERED
                || group.getStatus() == OrderSellerGroupStatus.CANCELLED) {
            throw new ConflictException(ErrorCode.ORDER_GROUP_INVALID_STATUS,
                    "Cannot cancel a group in " + group.getStatus() + " status");
        }

        group.setStatus(OrderSellerGroupStatus.CANCELLED);
        group.setCancellationReason(reason.trim());
        group.setCancelledAt(LocalDateTime.now());
        orderGroupDao.save(group);

        eventPublisher.publishEvent(buildStatusEvent(group));
    }

    @Override
    @Transactional
    public void removeShippingCost(Long storeId, Long groupId, String reason, Long userId) {
        resolveStoreAccess(storeId, userId);
        OrderSellerGroup group = resolveGroup(groupId, storeId);

        if (group.getStatus() == OrderSellerGroupStatus.DELIVERED
                || group.getStatus() == OrderSellerGroupStatus.CANCELLED) {
            throw new ConflictException(ErrorCode.ORDER_GROUP_INVALID_STATUS,
                    "Cannot remove shipping cost for a group in " + group.getStatus() + " status");
        }

        if (group.getShippingCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException(ErrorCode.ORDER_GROUP_SHIPPING_ALREADY_REMOVED,
                    "This group has no shipping cost to remove");
        }

        BigDecimal waivedAmount = group.getShippingCost();

        Order order = group.getOrder();
        order.setTotal(order.getTotal().subtract(waivedAmount));
        orderDao.save(order);

        group.setShippingCostWaived(waivedAmount);
        group.setShippingCost(BigDecimal.ZERO);
        group.setShippingRemovedAt(LocalDateTime.now());
        group.setShippingRemovedReason(reason.trim());
        group.setShippingRemovedByUserId(userId);
        orderGroupDao.save(group);

        eventPublisher.publishEvent(buildShippingRemovedEvent(group, waivedAmount, order.getTotal()));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private void resolveStoreAccess(Long storeId, Long userId) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED,
                        "You are not an active member of this store"));
    }

    private OrderSellerGroup resolveGroup(Long groupId, Long storeId) {
        OrderSellerGroup group = orderGroupDao.findByIdWithDetails(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_GROUP_NOT_FOUND, "Order group not found"));
        if (!group.getStore().getId().equals(storeId)) {
            throw new ForbiddenException(ErrorCode.ORDER_GROUP_ACCESS_DENIED, "Access denied to this order group");
        }
        return group;
    }

    private OrderStatusChangedEvent buildStatusEvent(OrderSellerGroup group) {
        String buyerEmail = group.getOrder().getBuyerEmail();
        String buyerName = group.getOrder().resolveContactName();

        List<OrderStatusChangedEvent.ItemData> items = group.getItems().stream()
                .map(i -> new OrderStatusChangedEvent.ItemData(
                        i.getVariant().getProduct().getTitle(),
                        i.getVariant().getSku(),
                        i.getQuantity()))
                .toList();

        return new OrderStatusChangedEvent(
                group.getId(),
                group.getOrder().getId(),
                buyerEmail,
                buyerName,
                group.getStatus(),
                group.getTrackingNumber(),
                group.getCancellationReason(),
                group.getOrder().getShippingAddress(),
                group.getOrder().getShippingCity(),
                group.getOrder().getShippingCountry(),
                items
        );
    }

    private ShippingCostRemovedEvent buildShippingRemovedEvent(OrderSellerGroup group, BigDecimal waivedAmount,
                                                                BigDecimal newOrderTotal) {
        return new ShippingCostRemovedEvent(
                group.getId(),
                group.getOrder().getId(),
                group.getOrder().getBuyerEmail(),
                group.getOrder().resolveContactName(),
                group.getStore().getName(),
                waivedAmount,
                newOrderTotal,
                group.getShippingRemovedReason()
        );
    }

    private SellerOrderGroupDto toSellerDto(OrderSellerGroup group) {
        String buyerName = group.getOrder().resolveContactName();

        List<OrderItemDto> items = group.getItems().stream()
                .map(i -> {
                    BigDecimal subtotal = i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
                    BigDecimal totalCostItem = i.getUnitCost() != null
                            ? i.getUnitCost().multiply(BigDecimal.valueOf(i.getQuantity()))
                            : null;
                    BigDecimal unitMargin = i.getUnitCost() != null
                            ? i.getUnitPrice().subtract(i.getUnitCost())
                            : null;
                    List<VariantAttributeDto> attributes = i.getVariant().getAttributeValues().stream()
                            .map(av -> new VariantAttributeDto(
                                    av.getAttribute().getDescription(),
                                    av.getDescription()))
                            .toList();
                    Product product = i.getVariant().getProduct();
                    return new OrderItemDto(
                            i.getId(),
                            i.getVariant().getId(),
                            i.getVariant().getSku(),
                            product.getTitle(),
                            product.getCategory().getDescription(),
                            product.getBrand().getDescription(),
                            product.getLine() != null ? product.getLine().getDescription() : null,
                            attributes,
                            i.getQuantity(),
                            i.getUnitPrice(),
                            subtotal,
                            i.getUnitCost(),
                            totalCostItem,
                            unitMargin);
                })
                .toList();

        boolean allHaveCost = items.stream().allMatch(i -> i.unitCost() != null);
        BigDecimal totalCost = allHaveCost
                ? items.stream().map(i -> i.totalCost()).reduce(BigDecimal.ZERO, BigDecimal::add)
                : null;
        BigDecimal totalMargin = totalCost != null ? group.getSubtotal().subtract(totalCost) : null;
        BigDecimal marginPercent = totalMargin != null && group.getSubtotal().compareTo(BigDecimal.ZERO) > 0
                ? totalMargin.divide(group.getSubtotal(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : null;

        return new SellerOrderGroupDto(
                group.getId(),
                group.getOrder().getId(),
                buyerName,
                group.getOrder().getBuyerPhone(),
                group.getOrder().getBuyerEmail(),
                group.getOrder().getShippingAddress(),
                group.getOrder().getShippingCity(),
                group.getOrder().getShippingCountry(),
                group.getOrder().getPaymentMethod(),
                group.getOrder().getStatus().name(),
                group.getStatus().name(),
                group.getSubtotal(),
                group.getShippingCost(),
                group.getOrder().getTotal(),
                group.getOrder().getCouponCode(),
                group.getOrder().getCouponDiscount(),
                group.getTrackingNumber(),
                group.getPreparedAt(),
                group.getShippedAt(),
                group.getDeliveredAt(),
                group.getCancelledAt(),
                group.getCancellationReason(),
                group.getShippingCostWaived(),
                group.getShippingRemovedAt(),
                group.getShippingRemovedReason(),
                items,
                totalCost,
                totalMargin,
                marginPercent
        );
    }
}
