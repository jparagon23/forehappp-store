package com.forehapp.store.notificationModule.infrastructure.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.forehapp.store.notificationModule.domain.model.FcmToken;
import com.forehapp.store.notificationModule.infrastructure.persistence.IFcmTokenJpaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(FcmNotificationService.class);

    private final IFcmTokenJpaRepository fcmTokenRepository;

    @Value("${fcm.service-account-json:#{null}}")
    private String serviceAccountJson;

    @PostConstruct
    void init() {
        if (serviceAccountJson == null || serviceAccountJson.isBlank() || !serviceAccountJson.trim().startsWith("{")) {
            logger.warn("[FCM] fcm.service-account-json not configured. FCM notifications disabled.");
            return;
        }
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(new java.io.ByteArrayInputStream(
                                serviceAccountJson.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                        .createScoped("https://www.googleapis.com/auth/firebase.messaging");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("[FCM] FirebaseApp initialized.");
            }
        } catch (Exception e) {
            logger.error("[FCM] Failed to initialize FirebaseApp: {}", e.getMessage(), e);
        }
    }

    public boolean sendToUser(Long userId, String title, String body, String actionUrl) {
        if (userId == null) return false;
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("[FCM] FirebaseApp not initialized. Skipping for userId={}", userId);
            return false;
        }

        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndActiveTrue(userId);
        if (tokens.isEmpty()) {
            logger.debug("[FCM] No active tokens for userId={}", userId);
            return false;
        }

        int ok = 0;
        for (FcmToken fcmToken : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(fcmToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putData("url", actionUrl != null ? actionUrl : "")
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                logger.debug("[FCM] Sent. userId={} messageId={}", userId, response);
                ok++;
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode code = e.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                    fcmToken.setActive(false);
                    fcmTokenRepository.save(fcmToken);
                    logger.info("[FCM] Deactivated invalid token for userId={} code={}", userId, code);
                } else {
                    logger.error("[FCM] Error userId={} code={} msg={}", userId, code, e.getMessage());
                }
            }
        }

        logger.info("[FCM] Result userId={} ok={} total={}", userId, ok, tokens.size());
        return ok > 0;
    }

    public void upsertToken(Long userId, String token) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("FCM token is required");
        FcmToken entity = fcmTokenRepository.findByToken(token).orElseGet(FcmToken::new);
        entity.setUserId(userId);
        entity.setToken(token);
        entity.setActive(true);
        fcmTokenRepository.save(entity);
    }

    public void deactivateToken(String token) {
        fcmTokenRepository.findByToken(token).ifPresent(e -> {
            e.setActive(false);
            fcmTokenRepository.save(e);
        });
    }
}
