package com.forehapp.store.paymentModule.application.usecases;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.orderModule.domain.events.OrderCreatedEvent;
import com.forehapp.store.orderModule.domain.events.OrderPaidEvent;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.domain.model.OrderStatus;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.orderModule.domain.ports.out.IOrderGroupDao;
import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import com.forehapp.store.paymentModule.domain.model.PaymentStatus;
import com.forehapp.store.paymentModule.domain.ports.in.IPaymentModuleService;
import com.forehapp.store.paymentModule.infrastructure.persistence.IPaymentRepository;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentModuleServiceImpl implements IPaymentModuleService {

    private static final Logger log = LoggerFactory.getLogger(PaymentModuleServiceImpl.class);

    private final IPaymentRepository paymentRepository;
    private final IOrderDao orderDao;
    private final IOrderGroupDao orderGroupDao;
    private final IStoreProfileDao storeProfileDao;
    private final IStoreMembershipDao membershipDao;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentModuleServiceImpl(IPaymentRepository paymentRepository,
                                    IOrderDao orderDao,
                                    IOrderGroupDao orderGroupDao,
                                    IStoreProfileDao storeProfileDao,
                                    IStoreMembershipDao membershipDao,
                                    ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.orderDao = orderDao;
        this.orderGroupDao = orderGroupDao;
        this.storeProfileDao = storeProfileDao;
        this.membershipDao = membershipDao;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void handlePaymentNotification(String externalPaymentId) {
        log.info("[Webhook] Processing payment notification. paymentId={}", externalPaymentId);

        Payment mpPayment;
        try {
            mpPayment = new PaymentClient().get(Long.parseLong(externalPaymentId));
        } catch (Exception e) {
            log.error("[Webhook] Error fetching payment from MercadoPago. paymentId={}", externalPaymentId, e);
            return;
        }

        String externalReference = mpPayment.getExternalReference();
        if (externalReference == null || externalReference.isBlank()) {
            log.warn("[Webhook] Missing externalReference. paymentId={}", externalPaymentId);
            return;
        }

        Long orderId;
        try {
            orderId = Long.parseLong(externalReference);
        } catch (NumberFormatException e) {
            log.error("[Webhook] externalReference is not a valid orderId: {}", externalReference);
            return;
        }

        var paymentOpt = paymentRepository.findByOrderId(orderId);
        if (paymentOpt.isEmpty()) {
            log.warn("[Webhook] No payment record found for orderId={}", orderId);
            return;
        }

        com.forehapp.store.paymentModule.domain.model.Payment payment = paymentOpt.get();

        if (PaymentStatus.APPROVED.name().equals(payment.getStatus())) {
            log.info("[Webhook] Payment already approved (idempotency). orderId={}", orderId);
            return;
        }

        String mpStatus = mpPayment.getStatus();
        log.info("[Webhook] MP status={} for orderId={}", mpStatus, orderId);

        switch (mpStatus) {
            case "approved" -> {
                payment.setStatus(PaymentStatus.APPROVED.name());
                payment.setReference(externalPaymentId);
                paymentRepository.save(payment);

                Order order = orderDao.findById(orderId).orElse(null);
                if (order != null) {
                    order.setStatus(OrderStatus.PAID);
                    orderDao.save(order);
                    log.info("[Webhook] Order {} marked as PAID", orderId);

                    transitionGroupsToPreparing(orderId);

                    String buyerEmail = order.getBuyerEmail();
                    String buyerName  = order.resolveContactName();

                    eventPublisher.publishEvent(new OrderPaidEvent(
                            order.getId(), buyerEmail, buyerName, order.getTotal(), order.getCreatedAt()
                    ));

                    eventPublisher.publishEvent(buildSellerNotificationEvent(order, buyerName, buyerEmail));
                }
            }
            case "rejected" -> {
                payment.setStatus(PaymentStatus.REJECTED.name());
                paymentRepository.save(payment);
                log.warn("[Webhook] Payment rejected for orderId={}", orderId);
            }
            case "refunded", "charged_back" -> {
                payment.setStatus(PaymentStatus.REFUNDED.name());
                paymentRepository.save(payment);

                Order order = orderDao.findBasicById(orderId).orElse(null);
                if (order != null) {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderDao.save(order);
                    log.warn("[Webhook] Order {} cancelled due to refund/chargeback", orderId);
                }
            }
            default -> log.info("[Webhook] Ignored MP status={} for orderId={}", mpStatus, orderId);
        }
    }

    @Override
    @Transactional
    public void confirmCashPayment(Long userId, Long orderId) {
        resolveAdmin(userId);

        Order order = orderDao.findBasicById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PAYMENT_ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException(ErrorCode.PAYMENT_ORDER_NOT_PENDING, "Order is not in PENDING status");
        }

        String method = order.getPaymentMethod();
        if (!PaymentMethod.CASH.name().equals(method) && !PaymentMethod.TRANSFER.name().equals(method)) {
            throw new BadRequestException(ErrorCode.PAYMENT_METHOD_MISMATCH,
                    "Only CASH or TRANSFER orders can be confirmed manually");
        }

        var paymentOpt = paymentRepository.findByOrderId(orderId);
        if (paymentOpt.isEmpty()) {
            throw new NotFoundException(ErrorCode.PAYMENT_RECORD_NOT_FOUND, "Payment record not found");
        }

        com.forehapp.store.paymentModule.domain.model.Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.APPROVED.name());
        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
        orderDao.save(order);
        log.info("[Admin] Cash payment confirmed for orderId={}", orderId);

        transitionGroupsToPreparing(orderId);

        String buyerEmail = order.getBuyerEmail();
        String buyerName  = order.resolveContactName();
        eventPublisher.publishEvent(new OrderPaidEvent(
                order.getId(), buyerEmail, buyerName, order.getTotal(), order.getCreatedAt()
        ));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private void transitionGroupsToPreparing(Long orderId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (OrderSellerGroup group : orderGroupDao.findAllByOrderId(orderId)) {
            if (group.getStatus() == OrderSellerGroupStatus.PENDING) {
                group.setStatus(OrderSellerGroupStatus.PREPARING);
                group.setPreparedAt(now);
                orderGroupDao.save(group);
                log.info("[Payment] Group {} transitioned to PREPARING", group.getId());
            }
        }
    }

    private void resolveAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.PAYMENT_ACCESS_DENIED, "Access denied: STORE_ADMIN role required");
        }
    }

    private OrderCreatedEvent buildSellerNotificationEvent(Order order, String buyerName, String buyerEmail) {
        java.util.List<OrderCreatedEvent.SellerGroupData> sellerGroups = order.getSellerGroups().stream()
                .map(group -> {
                    java.util.List<String> memberEmails = membershipDao.findActiveByStoreId(group.getStore().getId())
                            .stream()
                            .map(m -> m.getStoreProfile().getUser().getEmail())
                            .toList();
                    java.util.List<OrderCreatedEvent.ItemData> items = group.getItems().stream()
                            .map(item -> new OrderCreatedEvent.ItemData(
                                    item.getVariant().getProduct().getTitle(),
                                    item.getVariant().getSku(),
                                    item.getQuantity(),
                                    item.getUnitPrice(),
                                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                            ))
                            .toList();
                    return new OrderCreatedEvent.SellerGroupData(memberEmails, group.getStore().getName(),
                            group.getSubtotal(), items);
                })
                .toList();

        return new OrderCreatedEvent(
                order.getId(), buyerName, buyerEmail,
                order.getShippingAddress(), order.getShippingCity(), order.getShippingCountry(),
                order.getCreatedAt(), order.getTotal(), order.getPaymentMethod(),
                sellerGroups, true
        );
    }
}
