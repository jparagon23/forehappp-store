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

import com.forehapp.store.mail.EmailSender;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

@Service
public class PaymentModuleServiceImpl implements IPaymentModuleService {

    private static final Logger log = LoggerFactory.getLogger(PaymentModuleServiceImpl.class);

    private final IPaymentRepository paymentRepository;
    private final IOrderDao orderDao;
    private final IOrderGroupDao orderGroupDao;
    private final IStoreProfileDao storeProfileDao;
    private final IStoreMembershipDao membershipDao;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailSender emailSender;
    private final List<String> adminEmails;
    private final String currency;

    public PaymentModuleServiceImpl(IPaymentRepository paymentRepository,
                                    IOrderDao orderDao,
                                    IOrderGroupDao orderGroupDao,
                                    IStoreProfileDao storeProfileDao,
                                    IStoreMembershipDao membershipDao,
                                    ApplicationEventPublisher eventPublisher,
                                    EmailSender emailSender,
                                    @Value("${app.alert.admin-emails:}") String adminEmailsCsv,
                                    @Value("${app.payment.currency}") String currency) {
        this.paymentRepository = paymentRepository;
        this.orderDao = orderDao;
        this.orderGroupDao = orderGroupDao;
        this.storeProfileDao = storeProfileDao;
        this.membershipDao = membershipDao;
        this.eventPublisher = eventPublisher;
        this.emailSender = emailSender;
        this.adminEmails = Arrays.stream(adminEmailsCsv.split(","))
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .toList();
        this.currency = currency;
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
                if (mpPayment.getTransactionAmount() != null) payment.setAmount(mpPayment.getTransactionAmount());
                paymentRepository.save(payment);

                Order order = orderDao.findById(orderId).orElse(null);
                if (order != null) {
                    recordAmountDifference(order, mpPayment, externalPaymentId);
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
        confirmManualPayment(orderId, true);
    }

    @Override
    @Transactional
    public void confirmManualPayment(Long orderId, boolean notifyBuyer) {
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
        log.info("[Payment] Manual payment confirmed for orderId={}", orderId);

        transitionGroupsToPreparing(orderId);

        if (!notifyBuyer) return;
        String buyerEmail = order.getBuyerEmail();
        String buyerName  = order.resolveContactName();
        eventPublisher.publishEvent(new OrderPaidEvent(
                order.getId(), buyerEmail, buyerName, order.getTotal(), order.getCreatedAt()
        ));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    /**
     * The payment is approved, but it may not cover the order: an old link paid after the order changed,
     * or a different currency. Any difference goes to balanceDue (positive = buyer owes, negative = store
     * owes back), to be settled by hand like an edited paid order, and the admins get an email.
     */
    private void recordAmountDifference(Order order, Payment mpPayment, String externalPaymentId) {
        BigDecimal expected = MercadoPagoService.chargeAmount(order);
        boolean sameCurrency = currency.equalsIgnoreCase(String.valueOf(mpPayment.getCurrencyId()));
        BigDecimal paid = sameCurrency && mpPayment.getTransactionAmount() != null
                ? mpPayment.getTransactionAmount() : BigDecimal.ZERO;
        BigDecimal difference = expected.subtract(paid);
        if (difference.abs().compareTo(BigDecimal.ONE) < 0) return;

        BigDecimal balance = (order.getBalanceDue() == null ? BigDecimal.ZERO : order.getBalanceDue()).add(difference);
        order.setBalanceDue(balance.signum() == 0 ? null : balance);
        log.warn("[Webhook] Amount mismatch on order {}: expected {} {}, paid {} {} (MP payment {}). balanceDue={}",
                order.getId(), expected, currency, mpPayment.getTransactionAmount(), mpPayment.getCurrencyId(),
                externalPaymentId, order.getBalanceDue());
        alertAdmins("Pago con monto distinto - pedido #" + order.getId(),
                "<p>Mercado Pago aprobó un pago que no coincide con el total del pedido <b>#" + order.getId() + "</b>.</p>"
                + "<p>Total del pedido: " + expected + " " + currency + "<br>"
                + "Pagado: " + mpPayment.getTransactionAmount() + " " + HtmlUtils.htmlEscape(String.valueOf(mpPayment.getCurrencyId())) + "<br>"
                + "Pago de Mercado Pago: " + HtmlUtils.htmlEscape(externalPaymentId) + "</p>"
                + "<p>El pedido quedó pagado con saldo pendiente de <b>" + order.getBalanceDue() + "</b>"
                + " (positivo: lo debe el comprador; negativo: se le debe devolver). Revisalo antes de despachar.</p>");
    }

    private void alertAdmins(String subject, String html) {
        for (String email : adminEmails) {
            emailSender.sendEmail(email, subject, html).exceptionally(t -> {
                log.warn("[Webhook] Failed to send alert to {}: {}", email, t.getMessage());
                return null;
            });
        }
    }

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
                            group.getSubtotal(), group.getShippingCost(), items);
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
