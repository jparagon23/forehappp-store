package com.forehapp.store.repurchaseModule;

import com.forehapp.store.repurchaseModule.application.usecases.UnsubscribeTokenService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnsubscribeTokenServiceTest {

    private final UnsubscribeTokenService service = new UnsubscribeTokenService("test-secret");

    @Test
    void tokenRoundTripsToNormalizedEmail() {
        String token = service.createToken(" Buyer@Mail.com ");

        assertEquals(Optional.of("buyer@mail.com"), service.parseEmail(token));
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        String token = new UnsubscribeTokenService("other-secret").createToken("buyer@mail.com");

        assertTrue(service.parseEmail(token).isEmpty());
    }

    @Test
    void rejectsTokenWhoseEmailWasSwapped() {
        String signature = service.createToken("buyer@mail.com").split("\\.")[1];
        String otherEmail = service.createToken("victim@mail.com").split("\\.")[0];

        assertTrue(service.parseEmail(otherEmail + "." + signature).isEmpty());
    }

    @Test
    void rejectsMalformedTokens() {
        assertTrue(service.parseEmail(null).isEmpty());
        assertTrue(service.parseEmail("").isEmpty());
        assertTrue(service.parseEmail("not-a-token").isEmpty());
        assertTrue(service.parseEmail("%%%.%%%").isEmpty());
    }
}
