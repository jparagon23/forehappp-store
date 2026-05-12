package com.forehapp.store.mail;

import java.util.concurrent.CompletableFuture;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "mock", matchIfMissing = true)
public class MockEmailService implements EmailSender 
{

    @Override
public CompletableFuture<Void> sendEmail(String to, String subject, String html) {
    log.info("[MOCK EMAIL] Sending email");
    log.info("To: {}", to);
    log.info("Subject: {}", subject);
    log.info("HTML Content length: {} characters", html != null ? html.length() : 0);
    return CompletableFuture.completedFuture(null);
}
}