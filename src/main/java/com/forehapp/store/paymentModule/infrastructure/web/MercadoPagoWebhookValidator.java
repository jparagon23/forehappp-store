package com.forehapp.store.paymentModule.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class MercadoPagoWebhookValidator {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoWebhookValidator.class);

    @Value("${mercadopago.webhook.secret}")
    private String webhookSecret;

    public boolean isValid(String xSignature, String xRequestId, String dataId) {
        // Without the secret nothing can be verified, so nothing is accepted
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.error("[Webhook] MERCADOPAGO_WEBHOOK_SECRET is not configured — rejecting notification");
            return false;
        }

        if (xSignature == null || xSignature.isBlank()) {
            log.warn("[Webhook] Missing x-signature header");
            return false;
        }

        String ts = null;
        String v1 = null;
        for (String part : xSignature.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                String k = kv[0].trim();
                String v = kv[1].trim();
                if ("ts".equals(k)) ts = v;
                if ("v1".equals(k)) v1 = v;
            }
        }

        if (ts == null || v1 == null) {
            log.warn("[Webhook] Invalid x-signature format: {}", xSignature);
            return false;
        }

        String safeRequestId = (xRequestId != null) ? xRequestId : "";
        String secret = webhookSecret.trim();

        String[] variants = {
            "id:" + dataId + ";request-id:" + safeRequestId + ";ts:" + ts + ";",
            "id:" + dataId + ";request-id:" + safeRequestId + ";ts:" + ts,
            "id:" + dataId + ";ts:" + ts + ";",
            "id:" + dataId + ";ts:" + ts,
        };

        for (int i = 0; i < variants.length; i++) {
            String computed = hmacSHA256(variants[i], secret);
            if (MessageDigest.isEqual(v1.getBytes(StandardCharsets.UTF_8), computed.getBytes(StandardCharsets.UTF_8))) {
                return true;
            }
            log.debug("[Webhook] variant[{}] no match. computed={}", i, computed);
        }

        log.warn("[Webhook] Signature validation failed for dataId={}", dataId);
        return false;
    }

    private String hmacSHA256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] keyBytes = HexFormat.of().parseHex(secret);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
            mac.init(keySpec);
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalArgumentException e) {
            try {
                Mac mac2 = Mac.getInstance("HmacSHA256");
                SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                mac2.init(keySpec);
                return HexFormat.of().formatHex(mac2.doFinal(data.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception ex) {
                throw new RuntimeException("Error computing HMAC-SHA256", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error computing HMAC-SHA256", e);
        }
    }
}
