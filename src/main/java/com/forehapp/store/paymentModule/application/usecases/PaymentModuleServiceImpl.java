package com.forehapp.store.paymentModule.application.usecases;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
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
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentModuleServiceImpl implements IPaymentModuleService {

    private static final Logger log = LoggerFactory.getLogger(PaymentModuleServiceImpl.class);

    private final IPaymentRepository paymentRepository;
    private final IOrderDao orderDao;
    private final IOrderGroupDao orderGroupDao;
    private final IStoreProfileDao storeProfileDao;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentModuleServiceImpl(IPaymentRepository paymentRepository,
                                    IOrderDao orderDao,
                                    IOrderGroupDao orderGroupDao,
                                    IStoreProfileDao storeProfileDao,
                                    ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.orderDao = orderDao;
        this.orderGroupDao = orderGroupDao;
        this.storeProfileDao = storeProfileDao;
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

                Order order = orderDao.findBasicById(orderId).orElse(null);
                if (order != null) {
                    order.setStatus(OrderStatus.PAID);
                    orderDao.save(order);
                    log.info("[Webhook] Order {} marked as PAID", orderId);

                    transitionGroupsToPreparing(orderId);

                    String buyerEmail = order.getBuyer().getUser().getEmail();
                    String buyerName  = order.getBuyer().getUser().getName() + " " + order.getBuyer().getUser().getLastname();
                    eventPublisher.publishEvent(new OrderPaidEvent(
                            order.getId(), buyerEmail, buyerName, order.getTotal(), order.getCreatedAt()
                    ));
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not in PENDING status");
        }

        String method = order.getPaymentMethod();
        if (!PaymentMethod.CASH.name().equals(method) && !PaymentMethod.TRANSFER.name().equals(method)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only CASH or TRANSFER orders can be confirmed manually");
        }

        var paymentOpt = paymentRepository.findByOrderId(orderId);
        if (paymentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment record not found");
        }

        com.forehapp.store.paymentModule.domain.model.Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.APPROVED.name());
        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
        orderDao.save(order);
        log.info("[Admin] Cash payment confirmed for orderId={}", orderId);

        transitionGroupsToPreparing(orderId);

        String buyerEmail = order.getBuyer().getUser().getEmail();
        String buyerName  = order.getBuyer().getUser().getName() + " " + order.getBuyer().getUser().getLastname();
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: STORE_ADMIN role required");
        }
    }
}
