package com.forehapp.store.mail;

import java.util.concurrent.CompletableFuture;

public interface EmailSender {
    CompletableFuture<Void> sendEmail(String to, String subject, String html);
}
