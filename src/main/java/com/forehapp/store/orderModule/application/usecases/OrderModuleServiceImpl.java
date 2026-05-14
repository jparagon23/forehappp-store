package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.orderModule.domain.events.OrderStatusChangedEvent;
import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.domain.ports.in.IOrderModuleService;
import com.forehapp.store.orderModule.domain.ports.out.IOrderGroupDao;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderItemDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.SellerOrderGroupDto;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderModuleServiceImpl implements IOrderModuleService {

    private final IOrderGroupDao orderGroupDao;
    private final IStoreProfileDao storeProfileDao;
    private final ApplicationEventPublisher eventPublisher;

    public OrderModuleServiceImpl(IOrderGroupDao orderGroupDao,
                                  IStoreProfileDao storeProfileDao,
                                  ApplicationEventPublisher eventPublisher) {
        this.orderGroupDao = orderGroupDao;
        this.storeProfileDao = storeProfileDao;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerOrderGroupDto> getSellerGroups(Long userId) {
        StoreProfile seller = resolveSeller(userId);
        return orderGroupDao.findAllBySellerIdWithDetails(seller.getId()).stream()
                .map(this::toSellerDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SellerOrderGroupDto getSellerGroupById(Long userId, Long groupId) {
        StoreProfile seller = resolveSeller(userId);
        OrderSellerGroup group = resolveGroup(groupId, seller.getId());
        return toSellerDto(group);
    }

    @Override
    @Transactional
    public void prepareGroup(Long userId, Long groupId) {
        StoreProfile seller = resolveSeller(userId);
        OrderSellerGroup group = resolveGroup(groupId, seller.getId());

        if (group.getStatus() != OrderSellerGroupStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Group must be in PENDING status to start preparing");
        }

        group.setStatus(OrderSellerGroupStatus.PREPARING);
        group.setPreparedAt(LocalDateTime.now());
        orderGroupDao.save(group);

        eventPublisher.publishEvent(buildStatusEvent(group));
    }

    @Override
    @Transactional
    public void shipGroup(Long userId, Long groupId, String trackingNumber) {
        StoreProfile seller = resolveSeller(userId);
        OrderSellerGroup group = resolveGroup(groupId, seller.getId());

        if (group.getStatus() != OrderSellerGroupStatus.PREPARING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
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
    public void deliverGroup(Long userId, Long groupId) {
        StoreProfile seller = resolveSeller(userId);
        OrderSellerGroup group = resolveGroup(groupId, seller.getId());

        if (group.getStatus() != OrderSellerGroupStatus.SHIPPED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Group must be in SHIPPED status to confirm delivery");
        }

        group.setStatus(OrderSellerGroupStatus.DELIVERED);
        group.setDeliveredAt(LocalDateTime.now());
        orderGroupDao.save(group);

        eventPublisher.publishEvent(buildStatusEvent(group));
    }

    @Override
    @Transactional
    public void cancelGroup(Long userId, Long groupId, String reason) {
        StoreProfile seller = resolveSeller(userId);
        OrderSellerGroup group = resolveGroup(groupId, seller.getId());

        if (group.getStatus() == OrderSellerGroupStatus.SHIPPED
                || group.getStatus() == OrderSellerGroupStatus.DELIVERED
                || group.getStatus() == OrderSellerGroupStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot cancel a group in " + group.getStatus() + " status");
        }

        group.setStatus(OrderSellerGroupStatus.CANCELLED);
        group.setCancellationReason(reason.trim());
        group.setCancelledAt(LocalDateTime.now());
        orderGroupDao.save(group);

        eventPublisher.publishEvent(buildStatusEvent(group));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private StoreProfile resolveSeller(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.SELLER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have SELLER role");
        }
        return profile;
    }

    private OrderSellerGroup resolveGroup(Long groupId, Long sellerId) {
        OrderSellerGroup group = orderGroupDao.findByIdWithDetails(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order group not found"));
        if (!group.getSeller().getId().equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this order group");
        }
        return group;
    }

    private OrderStatusChangedEvent buildStatusEvent(OrderSellerGroup group) {
        String buyerEmail = group.getOrder().getBuyer().getUser().getEmail();
        String buyerName = group.getOrder().getBuyer().getUser().getName()
                + " " + group.getOrder().getBuyer().getUser().getLastname();

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

    private SellerOrderGroupDto toSellerDto(OrderSellerGroup group) {
        String buyerName = group.getOrder().getBuyer().getUser().getName()
                + " " + group.getOrder().getBuyer().getUser().getLastname();

        List<OrderItemDto> items = group.getItems().stream()
                .map(i -> {
                    BigDecimal subtotal = i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
                    return new OrderItemDto(
                            i.getId(),
                            i.getVariant().getId(),
                            i.getVariant().getSku(),
                            i.getVariant().getProduct().getTitle(),
                            i.getQuantity(),
                            i.getUnitPrice(),
                            subtotal);
                })
                .toList();

        return new SellerOrderGroupDto(
                group.getId(),
                group.getOrder().getId(),
                buyerName,
                group.getOrder().getShippingAddress(),
                group.getOrder().getShippingCity(),
                group.getOrder().getShippingCountry(),
                group.getStatus().name(),
                group.getSubtotal(),
                group.getTrackingNumber(),
                group.getPreparedAt(),
                group.getShippedAt(),
                group.getDeliveredAt(),
                group.getCancelledAt(),
                group.getCancellationReason(),
                items
        );
    }
}
