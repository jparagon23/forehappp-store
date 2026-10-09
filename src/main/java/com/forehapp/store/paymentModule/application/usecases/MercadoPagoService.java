package com.forehapp.store.paymentModule.application.usecases;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.paymentModule.domain.model.Payment;
import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import com.forehapp.store.paymentModule.domain.model.PaymentStatus;
import com.forehapp.store.paymentModule.infrastructure.persistence.IPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MercadoPagoService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${app.payment.currency}")
    private String currency;

    @Value("${app.payment.back-url.success:http://localhost:3000/orders/success}")
    private String successUrl;

    @Value("${app.payment.back-url.failure:http://localhost:3000/orders/failure}")
    private String failureUrl;

    @Value("${app.payment.back-url.pending:http://localhost:3000/orders/pending}")
    private String pendingUrl;

    @Value("${app.payment.notification-url:}")
    private String notificationUrl;

    private final IPaymentRepository paymentRepository;

    public MercadoPagoService(IPaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public String createPreference(Order order) {
        Preference preference = newPreference(order);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(PaymentMethod.MERCADO_PAGO.name());
        payment.setStatus(PaymentStatus.PENDING.name());
        payment.setAmount(order.getTotal());
        payment.setReference(preference.getId());
        paymentRepository.save(payment);

        return preference.getInitPoint();
    }

    /**
     * New checkout link for an unpaid order whose items changed. The order keeps a single payment record:
     * it is pointed at the new preference, and the old link is expired so it cannot be paid at the old amount
     * (if expiring fails, the webhook still records any difference in the amount paid).
     */
    public String refreshPreference(Order order, Payment pendingPayment) {
        expirePreference(pendingPayment.getReference());
        Preference preference = newPreference(order);
        pendingPayment.setAmount(order.getTotal());
        pendingPayment.setReference(preference.getId());
        paymentRepository.save(pendingPayment);
        return preference.getInitPoint();
    }

    /** What the checkout link charges: the order total in whole pesos (COP has no cents; rounded up). */
    public static BigDecimal chargeAmount(Order order) {
        return order.getTotal().setScale(0, RoundingMode.UP);
    }

    private static String itemsSummary(Order order) {
        String summary = order.getSellerGroups().stream()
                .flatMap(group -> group.getItems().stream())
                .map(i -> i.getQuantity() + " x " + i.getVariant().getProduct().getTitle())
                .collect(Collectors.joining(", "));
        return summary.length() > 250 ? summary.substring(0, 247) + "..." : summary;
    }

    /** Best effort: a link that can no longer be paid. Failing here must not block the new link. */
    private void expirePreference(String preferenceId) {
        if (preferenceId == null || preferenceId.isBlank()) return;
        OffsetDateTime now = OffsetDateTime.now();
        try {
            new PreferenceClient().update(preferenceId, PreferenceRequest.builder()
                    .expires(true)
                    .expirationDateFrom(now.minusYears(1))
                    .expirationDateTo(now)
                    .build());
            log.info("[MP] Expired old preference {}", preferenceId);
        } catch (MPApiException e) {
            log.warn("[MP] Could not expire preference {}. status={} response={}", preferenceId,
                    e.getStatusCode(), e.getApiResponse() != null ? e.getApiResponse().getContent() : "null");
        } catch (MPException e) {
            log.warn("[MP] Could not expire preference {}: {}", preferenceId, e.getMessage());
        }
    }

    private Preference newPreference(Order order) {
        try {
            return new PreferenceClient().create(preferenceRequest(order));
        } catch (MPApiException e) {
            log.error("[MP] API error creating preference. status={} response={}",
                    e.getStatusCode(), e.getApiResponse() != null ? e.getApiResponse().getContent() : "null");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error creating payment preference: " + e.getStatusCode() + " - "
                    + (e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage()));
        } catch (MPException e) {
            log.error("[MP] SDK error creating preference", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error creating payment preference: " + e.getMessage());
        }
    }

    PreferenceRequest preferenceRequest(Order order) {
        // One line for the whole order: its total already has shipping, the coupon discount and the
        // Mercado Pago surcharge, which per-product lines cannot express (no negative prices).
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(order.getId().toString())
                .title("Pedido #" + order.getId() + " - ForehApp Store")
                .description(itemsSummary(order))
                .quantity(1)
                .unitPrice(chargeAmount(order))
                .currencyId(currency)
                .build();

        PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                .items(List.of(item))
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(successUrl + "?order_id=" + order.getId())
                        .failure(failureUrl + "?order_id=" + order.getId())
                        .pending(pendingUrl + "?order_id=" + order.getId())
                        .build())
                .externalReference(order.getId().toString())
                .autoReturn("approved");

        if (notificationUrl != null && !notificationUrl.isBlank()) {
            builder.notificationUrl(notificationUrl);
        }
        return builder.build();
    }
}
