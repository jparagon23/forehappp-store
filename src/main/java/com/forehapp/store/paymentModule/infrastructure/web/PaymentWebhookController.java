package com.forehapp.store.paymentModule.infrastructure.web;

import com.forehapp.store.paymentModule.domain.ports.in.IPaymentModuleService;
import com.forehapp.store.paymentModule.infrastructure.web.dto.MercadoPagoWebhookDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final IPaymentModuleService paymentModuleService;
    private final MercadoPagoWebhookValidator webhookValidator;

    public PaymentWebhookController(IPaymentModuleService paymentModuleService,
                                    MercadoPagoWebhookValidator webhookValidator) {
        this.paymentModuleService = paymentModuleService;
        this.webhookValidator = webhookValidator;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam(value = "topic", required = false) String urlParamTopic) {

        try {
            String type = (String) payload.getOrDefault("type", urlParamTopic);

            if (!payload.containsKey("data")) {
                log.debug("[Webhook] No data body, type={}", type);
                return ResponseEntity.ok().build();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null || data.get("id") == null) {
                log.warn("[Webhook] Missing data.id");
                return ResponseEntity.badRequest().build();
            }

            String paymentId = data.get("id").toString();

            if (!webhookValidator.isValid(xSignature, xRequestId, paymentId)) {
                log.warn("[Webhook] Invalid signature for paymentId={}", paymentId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if ("payment".equals(type)) {
                log.info("[Webhook] Payment notification received. paymentId={}", paymentId);
                paymentModuleService.handlePaymentNotification(paymentId);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("[Webhook] Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
