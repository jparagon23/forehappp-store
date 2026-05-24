package com.forehapp.store.mail;

import jakarta.annotation.PostConstruct;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.email.provider", havingValue = "smtp")
public class SmtpEmailService implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final Environment env;

    @Value("${spring.mail.username:}")
    private String fromMail;

    @Value("${app.mail.fromName:Forehapp Store}")
    private String fromName;

    @PostConstruct
    void init() {
        log.info("[SmtpEmailService] Initializing SMTP bean (provider=smtp)...");
        if (mailSender instanceof JavaMailSenderImpl jms) {
            Properties p = jms.getJavaMailProperties();
            log.info("[SmtpEmailService] host={}, port={}, username={}, protocol={}",
                    safe(jms.getHost()), jms.getPort(), safe(jms.getUsername()), safe(jms.getProtocol()));
            log.info("[SmtpEmailService] props: auth={}, starttls={}, ssl.enable={}, debug={}",
                    p.getProperty("mail.smtp.auth"),
                    p.getProperty("mail.smtp.starttls.enable"),
                    p.getProperty("mail.smtp.ssl.enable"),
                    p.getProperty("mail.debug"));
        } else {
            log.warn("[SmtpEmailService] mailSender is not JavaMailSenderImpl, type={}", mailSender.getClass().getName());
        }
        log.info("[SmtpEmailService] fromMail={}, fromName={}", safe(fromMail), safe(fromName));
    }

    @Override
    @Async
    public CompletableFuture<Void> sendEmail(String to, String subject, String html) {
        final String tid = "smtp-" + System.nanoTime();
        final Instant start = Instant.now();
        log.info("[{}] → sendEmail(to={}, subjectLen={}, htmlLen={})",
                tid, maskEmail(to), subject == null ? 0 : subject.length(), html == null ? 0 : html.length());

        try {
            if (fromMail == null || fromMail.isBlank()) {
                throw new IllegalStateException("spring.mail.username not configured");
            }
            try {
                new InternetAddress(fromMail, true);
                new InternetAddress(to, true);
            } catch (MessagingException addrEx) {
                log.error("[{}] Invalid address: fromMail='{}', to='{}'", tid, fromMail, to, addrEx);
                throw addrEx;
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setValidateAddresses(true);

            String plain = htmlToPlain(html);
            helper.setFrom(new InternetAddress(fromMail, fromName));
            helper.setTo(to);
            helper.setSubject(subject == null ? "" : subject);
            helper.setText(plain, html);

            mimeMessage.setHeader("X-Env", env.getProperty("spring.profiles.active", "default"));
            mimeMessage.setHeader("X-App", "Forehapp");
            mimeMessage.setHeader("List-Unsubscribe", "<mailto:unsubscribe@forehapp.com>, <https://forehapp.com/u>");
            mimeMessage.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");

            if (mailSender instanceof JavaMailSenderImpl jms) {
                log.info("[{}] Sending via host={}, port={}, user={}",
                        tid, safe(jms.getHost()), jms.getPort(), safe(jms.getUsername()));
            }

            mailSender.send(mimeMessage);

            Duration took = Duration.between(start, Instant.now());
            log.info("[{}] ✔ Email sent. to={}, subject='{}', took={} ms",
                    tid, maskEmail(to), safeSubject(subject), took.toMillis());
            return CompletableFuture.completedFuture(null);

        } catch (SendFailedException sfe) {
            log.error("[{}] ✖ SendFailedException: {}", tid, sfe.getMessage(), sfe);
            Address[] invalid = sfe.getInvalidAddresses();
            Address[] validUnsent = sfe.getValidUnsentAddresses();
            Address[] validSent = sfe.getValidSentAddresses();
            if (invalid != null)     log.error("[{}] invalidAddresses={}", tid, Arrays.toString(invalid));
            if (validUnsent != null) log.error("[{}] validUnsentAddresses={}", tid, Arrays.toString(validUnsent));
            if (validSent != null)   log.warn("[{}] validSentAddresses (partial)={}", tid, Arrays.toString(validSent));
            return CompletableFuture.failedFuture(sfe);

        } catch (MessagingException me) {
            log.error("[{}] ✖ MessagingException: {}", tid, me.getMessage(), me);
            if (mailSender instanceof JavaMailSenderImpl jms) {
                Properties p = jms.getJavaMailProperties();
                log.error("[{}] Effective SMTP: host={}, port={}, user={}, auth={}, starttls={}, ssl={}",
                        tid, safe(jms.getHost()), jms.getPort(), safe(jms.getUsername()),
                        p.getProperty("mail.smtp.auth"),
                        p.getProperty("mail.smtp.starttls.enable"),
                        p.getProperty("mail.smtp.ssl.enable"));
            }
            return CompletableFuture.failedFuture(me);

        } catch (Exception e) {
            log.error("[{}] ✖ Unexpected error: {}", tid, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private static String htmlToPlain(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private static String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }

    private static String safe(Object v) {
        return v == null ? "<null>" : String.valueOf(v);
    }

    private static String safeSubject(String s) {
        if (s == null) return "";
        return s.length() > 120 ? s.substring(0, 120) + "…" : s;
    }
}
