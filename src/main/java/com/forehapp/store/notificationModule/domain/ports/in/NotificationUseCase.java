package com.forehapp.store.notificationModule.domain.ports.in;

import com.forehapp.store.notificationModule.domain.model.NotificationType;

import java.util.List;

public interface NotificationUseCase {
    void sendEmailNotification(String to, String subject, String message);
    boolean sendPushNotification(Long userId, String title, String message, String actionUrl, NotificationType type);
    void sendPushNotificationFireAndForget(Long userId, String title, String message, String actionUrl, NotificationType type);
    void sendPushNotification(List<Long> userIds, String title, String message, String actionUrl, NotificationType type);
    boolean hasActivePushSubscription(Long userId);
}
