package com.forehapp.store.repurchaseModule.application.usecases;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

/** Stateless unsubscribe tokens: the email plus an HMAC signature, so no login or token table is needed. */
@Component
public class UnsubscribeTokenService {

    private static final String ALGORITHM = "HmacSHA256";
    // Keeps these signatures from being valid for any other use of the same secret
    private static final String PURPOSE = "email-unsubscribe:";

    private final byte[] secret;

    public UnsubscribeTokenService(@Value("${app.repurchase.unsubscribe-secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.repurchase.unsubscribe-secret not configured");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String createToken(String email) {
        String normalized = ReminderSelector.normalizeEmail(email);
        return encode(normalized.getBytes(StandardCharsets.UTF_8)) + "." + encode(sign(normalized));
    }

    public Optional<String> parseEmail(String token) {
        if (token == null) return Optional.empty();
        String[] parts = token.trim().split("\\.");
        if (parts.length != 2) return Optional.empty();
        try {
            String email = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            byte[] signature = Base64.getUrlDecoder().decode(parts[1]);
            if (email.isBlank() || !MessageDigest.isEqual(signature, sign(email))) {
                return Optional.empty();
            }
            return Optional.of(email);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private byte[] sign(String email) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            return mac.doFinal((PURPOSE + email).getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to sign unsubscribe token", e);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
