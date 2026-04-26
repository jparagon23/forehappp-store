package com.forehapp.store.notificationModule.infrastructure.services;

import tools.jackson.databind.ObjectMapper;
import com.forehapp.store.notificationModule.application.dto.PushSubscriptionDto;
import com.forehapp.store.notificationModule.domain.model.NotificationType;
import com.forehapp.store.notificationModule.domain.model.PushSubscription;
import com.forehapp.store.notificationModule.infrastructure.persistence.IPushSubscriptionJpaRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nl.martijndwars.webpush.PushService;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    private final IPushSubscriptionJpaRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${push.vapid.publicKey}")
    private String vapidPublicKey;

    @Value("${push.vapid.privateKey}")
    private String vapidPrivateKey;

    @Value("${push.vapid.subject:mailto:admin@forehapp.com}")
    private String vapidSubject;

    public void saveSubscription(Long userId, PushSubscriptionDto dto) {
        validate(dto);
        PushSubscription sub = subscriptionRepository.findByEndpoint(dto.getEndpoint())
                .orElseGet(PushSubscription::new);
        sub.setUserId(userId);
        sub.setEndpoint(dto.getEndpoint());
        sub.setP256dh(dto.getKeys().getP256dh());
        sub.setAuth(dto.getKeys().getAuth());
        sub.setActive(true);
        sub.setExpirationTime(dto.getExpirationTime());
        subscriptionRepository.save(sub);
        logger.info("[Push] Subscription saved. userId={} endpointHash={}", userId, hashEndpoint(dto.getEndpoint()));
    }

    public boolean hasActiveSubscription(Long userId) {
        return userId != null && subscriptionRepository.existsByUserIdAndActiveTrue(userId);
    }

    public boolean sendToUser(Long userId, String title, String body, String actionUrl, NotificationType type) {
        if (userId == null) return false;

        List<PushSubscription> subs = subscriptionRepository.findByUserIdAndActiveTrue(userId);
        if (subs == null || subs.isEmpty()) {
            logger.debug("[Push] No active subscriptions for userId={}", userId);
            return false;
        }

        PushService pushService = buildPushService();

        PushPayload payload = new PushPayload();
        payload.setTitle(title);
        payload.setBody(body);
        payload.setUrl(actionUrl);
        payload.setType(type != null ? type.name() : null);

        byte[] jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsBytes(payload);
        } catch (Exception e) {
            logger.error("[Push] Payload serialization failed. userId={}", userId, e);
            return false;
        }

        int ok = 0, gone = 0, failed = 0;

        for (PushSubscription sub : subs) {
            String endpointHash = hashEndpoint(sub.getEndpoint());
            try {
                nl.martijndwars.webpush.Notification notification =
                        new nl.martijndwars.webpush.Notification(
                                sub.getEndpoint(), sub.getP256dh(), sub.getAuth(), jsonPayload);

                org.apache.http.HttpResponse response = pushService.send(notification);
                if (response == null || response.getStatusLine() == null) {
                    failed++;
                    continue;
                }

                int status = response.getStatusLine().getStatusCode();

                if (response.getEntity() != null) {
                    if (status < 200 || status >= 300) {
                        EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    } else {
                        EntityUtils.consumeQuietly(response.getEntity());
                    }
                }

                if (status == 404 || status == 410) {
                    gone++;
                    sub.setActive(false);
                    subscriptionRepository.save(sub);
                    logger.info("[Push] Deactivated subscription. userId={} status={}", userId, status);
                } else if (status >= 200 && status < 300) {
                    ok++;
                } else {
                    failed++;
                    logger.warn("[Push] Rejected. status={} userId={} endpointHash={}", status, userId, endpointHash);
                }
            } catch (Exception e) {
                failed++;
                logger.error("[Push] Exception userId={} endpointHash={}", userId, endpointHash, e);
            }
        }

        logger.info("[Push] Result userId={} ok={} gone={} failed={} total={}", userId, ok, gone, failed, subs.size());
        return ok > 0;
    }

    public void unsubscribeByEndpoint(Long userId, String endpoint) {
        if (endpoint == null || endpoint.isBlank()) return;
        subscriptionRepository.findByEndpoint(endpoint).ifPresent(sub -> {
            if (userId != null && !userId.equals(sub.getUserId())) return;
            sub.setActive(false);
            sub.setUpdatedAt(LocalDateTime.now());
            subscriptionRepository.save(sub);
            logger.info("[Push] Unsubscribed endpointHash={} userId={}", hashEndpoint(endpoint), sub.getUserId());
        });
    }

    private PushService buildPushService() {
        try {
            PushService ps = new PushService();
            ps.setPublicKey(vapidPublicKey);
            ps.setPrivateKey(vapidPrivateKey);
            ps.setSubject(vapidSubject);
            return ps;
        } catch (Exception e) {
            throw new IllegalStateException("PushService init failed. Check VAPID keys.", e);
        }
    }

    private void validate(PushSubscriptionDto dto) {
        if (dto == null || dto.getEndpoint() == null || dto.getEndpoint().isBlank())
            throw new IllegalArgumentException("subscription.endpoint is required");
        if (dto.getKeys() == null || dto.getKeys().getP256dh() == null || dto.getKeys().getAuth() == null)
            throw new IllegalArgumentException("subscription.keys are required");
    }

    private String hashEndpoint(String endpoint) {
        return Integer.toHexString(endpoint != null ? endpoint.hashCode() : 0);
    }

    @Getter @Setter
    private static class PushPayload {
        private String title;
        private String body;
        private String url;
        private String type;
    }
}
