package com.forehapp.store.notificationModule.application.usecases;

import com.forehapp.store.mail.EmailSender;
import com.forehapp.store.notificationModule.application.dto.EmailTask;
import com.forehapp.store.notificationModule.application.dto.PushTask;
import com.forehapp.store.notificationModule.domain.model.NotificationType;
import com.forehapp.store.notificationModule.domain.ports.in.NotificationUseCase;
import com.forehapp.store.notificationModule.infrastructure.services.FcmNotificationService;
import com.forehapp.store.notificationModule.infrastructure.services.PushNotificationService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class NotificationUseCasesImpl implements NotificationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(NotificationUseCasesImpl.class);

    private final EmailSender emailSender;
    private final PushNotificationService pushNotificationService;
    private final FcmNotificationService fcmNotificationService;

    private final BlockingQueue<EmailTask> emailQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<PushTask>  pushQueue  = new LinkedBlockingQueue<>();

    public NotificationUseCasesImpl(EmailSender emailSender,
                                    PushNotificationService pushNotificationService,
                                    FcmNotificationService fcmNotificationService) {
        this.emailSender = emailSender;
        this.pushNotificationService = pushNotificationService;
        this.fcmNotificationService = fcmNotificationService;
    }

    @PostConstruct
    public void initEmailWorker() {
        final int BATCH_SIZE = 10;
        final long DELAY_MS = 200;
        final long BATCH_DELAY_MS = 800;

        Thread worker = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    List<EmailTask> batch = new ArrayList<>(BATCH_SIZE);
                    batch.add(emailQueue.take());
                    emailQueue.drainTo(batch, BATCH_SIZE - 1);
                    logger.info("[EmailWorker] Batch size={}", batch.size());
                    for (EmailTask task : batch) {
                        sendWithRetry(task, 3);
                        sleep(DELAY_MS);
                    }
                    sleep(BATCH_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("[EmailWorker] interrupted");
                } catch (Exception e) {
                    logger.error("[EmailWorker] unexpected error", e);
                }
            }
        });
        worker.setName("email-worker");
        worker.setDaemon(false);
        worker.start();
    }

    @PostConstruct
    public void initPushWorker() {
        final int BATCH_SIZE = 10;
        final long DELAY_MS = 100;
        final long BATCH_DELAY_MS = 600;

        Thread worker = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    List<PushTask> batch = new ArrayList<>(BATCH_SIZE);
                    batch.add(pushQueue.take());
                    pushQueue.drainTo(batch, BATCH_SIZE - 1);
                    logger.info("[PushWorker] Batch size={}", batch.size());
                    for (PushTask task : batch) {
                        dispatchPush(task);
                        sleep(DELAY_MS);
                    }
                    sleep(BATCH_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("[PushWorker] interrupted");
                } catch (Exception e) {
                    logger.error("[PushWorker] unexpected error", e);
                }
            }
        });
        worker.setName("push-worker");
        worker.setDaemon(false);
        worker.start();
    }

    @Override
    public void sendEmailNotification(String to, String subject, String message) {
        try {
            emailQueue.put(new EmailTask(to, subject, message));
            logger.info("Email enqueued for: {}", to);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while enqueuing email to: {}", to, e);
        }
    }

    @Override
    public boolean sendPushNotification(Long userId, String title, String message, String actionUrl, NotificationType type) {
        if (userId == null) return false;
        boolean vapid = false, fcm = false;
        try {
            vapid = pushNotificationService.sendToUser(userId, title, message, actionUrl, type);
        } catch (Exception e) {
            logger.error("[Push] VAPID error userId={}: {}", userId, e.getMessage(), e);
        }
        try {
            fcm = fcmNotificationService.sendToUser(userId, title, message, actionUrl);
        } catch (Exception e) {
            logger.error("[Push] FCM error userId={}: {}", userId, e.getMessage(), e);
        }
        return vapid || fcm;
    }

    @Async
    @Override
    public void sendPushNotificationFireAndForget(Long userId, String title, String message,
                                                  String actionUrl, NotificationType type) {
        sendPushNotification(userId, title, message, actionUrl, type);
    }

    @Override
    public void sendPushNotification(List<Long> userIds, String title, String message,
                                     String actionUrl, NotificationType type) {
        if (userIds == null || userIds.isEmpty()) return;
        for (Long userId : userIds) {
            if (userId == null) continue;
            try {
                pushQueue.put(new PushTask(userId, title, message, actionUrl, type));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("[Push] Interrupted enqueuing userId={}", userId, e);
                return;
            }
        }
        logger.info("[Push] Enqueued {} push notifications", userIds.size());
    }

    @Override
    public boolean hasActivePushSubscription(Long userId) {
        return userId != null && pushNotificationService.hasActiveSubscription(userId);
    }

    private void dispatchPush(PushTask task) {
        try {
            boolean vapid = pushNotificationService.sendToUser(task.userId(), task.title(), task.message(), task.actionUrl(), task.type());
            boolean fcm   = fcmNotificationService.sendToUser(task.userId(), task.title(), task.message(), task.actionUrl());
            logger.debug("[PushWorker] userId={} vapid={} fcm={}", task.userId(), vapid, fcm);
        } catch (Exception e) {
            logger.error("[PushWorker] Error userId={}: {}", task.userId(), e.getMessage(), e);
        }
    }

    private void sendWithRetry(EmailTask task, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                emailSender.sendEmail(task.to(), task.subject(), task.message()).get();
                return;
            } catch (Exception e) {
                logger.warn("[EmailWorker] Fail attempt {}/{} to={}", attempt, maxAttempts, task.to(), e);
                sleep(400L * attempt);
            }
        }
        logger.error("[EmailWorker] Giving up after {} attempts to={}", maxAttempts, task.to());
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
