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
import com.forehapp.store.paymentModule.domain.ports.in.IPaymentService;
import com.forehapp.store.paymentModule.infrastructure.persistence.IPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.util.List;

@Service
public class MercadoPagoService implements IPaymentService {

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

    @Override
    public String createMercadoPagoPreference(Order order) {
        List<PreferenceItemRequest> items = order.getSellerGroups().stream()
                .flatMap(group -> group.getItems().stream())
                .map(item -> PreferenceItemRequest.builder()
                        .title(item.getVariant().getProduct().getTitle())
                        .quantity(item.getQuantity())
                        // COP has no cents — round up to avoid MP rejecting fractional values
                        .unitPrice(item.getUnitPrice().setScale(0, RoundingMode.UP))
                        .currencyId(currency)
                        .build())
                .toList();

        PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                .items(items)
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

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(builder.build());

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setMethod(PaymentMethod.MERCADOPAGO.name());
            payment.setStatus(PaymentStatus.PENDIENTE.name());
            payment.setAmount(order.getTotal());
            payment.setReference(preference.getId());
            paymentRepository.save(payment);

            return preference.getInitPoint();

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
}
