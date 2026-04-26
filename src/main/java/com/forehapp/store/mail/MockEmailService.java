package com.forehapp.store.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "mock", matchIfMissing = true)
public class MockEmailService implements EmailSender {

    @Override
    public CompletableFuture<Void> sendEmail(String to, String subject, String html) {
        log.info("[MOCK EMAIL] to={} | subject={}", to, subject);
        return CompletableFuture.completedFuture(null);
    }
}
