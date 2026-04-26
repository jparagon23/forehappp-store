package com.forehapp.store.mail;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Primary
@ConditionalOnProperty(name = "app.email.provider", havingValue = "mailersend")
public class MailerSendEmailService implements EmailSender {

    private final MailerSend mailerSend = new MailerSend();
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    public MailerSendEmailService(
            @Value("${mailersend.apiKey}") String apiKey,
            @Value("${mailersend.fromEmail}") String fromEmail,
            @Value("${mailersend.fromName:Forehapp Store}") String fromName) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName.isBlank() ? fromEmail : fromName;
    }

    @PostConstruct
    void init() {
        mailerSend.setToken(apiKey);
        log.info("MailerSendEmailService (store) initialized with from={}", fromEmail);
    }

    @Override
    public CompletableFuture<Void> sendEmail(String to, String subject, String html) {
        log.info("Sending email to: {}", to);
        try {
            Email email = new Email();
            email.setFrom(fromName, fromEmail);
            email.addRecipient("", to);
            email.setSubject(subject);
            email.setHtml(html);
            email.setPlain(html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim());
            MailerSendResponse resp = mailerSend.emails().send(email);
            log.info("Email sent, messageId={}", resp.messageId);
            return CompletableFuture.completedFuture(null);
        } catch (MailerSendException e) {
            log.error("MailerSend error: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
