package com.forehapp.store.security;

import com.forehapp.store.authModule.application.services.AuthSessionService;
import com.forehapp.store.authModule.domain.model.AuthSession;
import com.forehapp.store.authModule.domain.ports.out.AuthSessionRepository;
import com.forehapp.store.general.constants.Constants;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.paymentModule.application.usecases.MercadoPagoService;
import com.forehapp.store.paymentModule.infrastructure.web.MercadoPagoWebhookValidator;
import com.forehapp.store.security.jwt.JwtUtil;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionsAndPaymentsTest {

    private final Map<String, AuthSession> sessions = new HashMap<>();
    private final User user = new User();
    private AuthSessionService service;

    @BeforeAll
    static void jwt() {
        JwtUtil jwt = new JwtUtil();
        ReflectionTestUtils.setField(jwt, "jwtSecret", "test-secret-test-secret-test-secret-1234");
        ReflectionTestUtils.setField(jwt, "accessTokenValiditySeconds", 1800L);
        ReflectionTestUtils.setField(jwt, "refreshTokenValiditySeconds", 604800L);
        jwt.init();
    }

    @BeforeEach
    void setUp() {
        user.setId(7L);
        user.setUserStatus(Constants.ACTIVE_USER_STATUS);
        user.setRoles(List.of());
        UserRepository users = mock(UserRepository.class);
        when(users.findById(anyLong())).thenAnswer(inv -> inv.getArgument(0).equals(7L) ? Optional.of(user) : Optional.empty());
        AuthSessionRepository repo = new AuthSessionRepository() {
            public AuthSession save(AuthSession s) { sessions.put(s.getId(), s); return s; }
            public Optional<AuthSession> findById(String id) { return Optional.ofNullable(sessions.get(id)); }
        };
        service = new AuthSessionService(repo, users);
    }

    private static String sessionOf(String accessToken) {
        return (String) JwtUtil.getAuthentication(accessToken).getDetails();
    }

    @Test
    void refreshWorksWhileTheSessionIsOpen() {
        var tokens = service.open(user);
        assertEquals("7", JwtUtil.getAuthentication(tokens.accessToken()).getPrincipal());
        assertTrue(service.isActive(sessionOf(tokens.accessToken())));

        var refreshed = service.refresh(tokens.refreshToken()).orElseThrow();
        assertEquals(sessionOf(tokens.accessToken()), sessionOf(refreshed.accessToken()));
    }

    @Test
    void logoutClosesTheSession() {
        var tokens = service.open(user);
        String sid = sessionOf(tokens.accessToken());
        assertTrue(service.isActive(sid)); // cached as open

        service.close(tokens.refreshToken());

        assertFalse(service.isActive(sid), "access token stops working right away on this server");
        assertTrue(service.refresh(tokens.refreshToken()).isEmpty());
        // Other logins of the same user are untouched
        var other = service.open(user);
        assertTrue(service.refresh(other.refreshToken()).isPresent());
    }

    @Test
    void inactiveUserCannotRefresh() {
        var tokens = service.open(user);
        user.setUserStatus(Constants.PENDING_STATUS);

        assertTrue(service.refresh(tokens.refreshToken()).isEmpty());
        assertFalse(service.isActive(sessionOf(tokens.accessToken())), "the session is closed too");
    }

    @Test
    void preRegisteredForehappUserKeepsTheSession() {
        // Created by a ForehApp organizer, logged in to the store with Google
        user.setUserStatus(Constants.PRE_REGISTER_STATUS);
        var tokens = service.open(user);
        assertTrue(service.refresh(tokens.refreshToken()).isPresent());
    }

    @Test
    void accountDeletedInForehappEndsTheSession() {
        var tokens = service.open(user);
        user.setUserStatus(5); // ForehApp "delete account"
        assertTrue(service.refresh(tokens.refreshToken()).isEmpty());
    }

    @Test
    void passwordChangeEndsTheSession() {
        user.setPassword("$2a$10$old");
        var tokens = service.open(user);
        var refreshed = service.refresh(tokens.refreshToken()).orElseThrow();

        user.setPassword("$2a$10$new"); // e.g. password recovery in ForehApp
        assertTrue(service.refresh(refreshed.refreshToken()).isEmpty());
        assertFalse(service.isActive(sessionOf(refreshed.accessToken())));
    }

    @Test
    void sessionWithoutFingerprintGetsOneInsteadOfEnding() {
        user.setPassword("$2a$10$same");
        var tokens = service.open(user);
        AuthSession session = sessions.get(sessionOf(tokens.accessToken()));
        session.setPasswordFingerprint(null); // opened before the column existed

        var refreshed = service.refresh(tokens.refreshToken());
        assertTrue(refreshed.isPresent());
        assertNotNull(session.getPasswordFingerprint());
        user.setPassword("$2a$10$changed");
        assertTrue(service.refresh(refreshed.get().refreshToken()).isEmpty());
    }

    @Test
    void tokensWithoutSessionOrOfTheWrongTypeAreRejected() {
        var tokens = service.open(user);
        // An access token is not a refresh token
        assertTrue(service.refresh(tokens.accessToken()).isEmpty());
        assertTrue(service.refresh("not-a-token").isEmpty());
        // Unknown session id (e.g. issued by another database)
        String foreign = JwtUtil.createRefreshToken("7", List.of(), "00000000-0000-0000-0000-000000000000");
        assertTrue(service.refresh(foreign).isEmpty());
    }

    @Test
    void webhookWithoutSecretIsRejected() {
        MercadoPagoWebhookValidator validator = new MercadoPagoWebhookValidator();
        ReflectionTestUtils.setField(validator, "webhookSecret", "");
        assertFalse(validator.isValid("ts=1,v1=abc", "req", "123"));
    }

    @Test
    void checkoutLinkChargesTheOrderTotal() {
        Order order = new Order();
        // products + shipping - coupon + 3.5% surcharge, with cents
        order.setTotal(new BigDecimal("103500.40"));
        assertEquals(new BigDecimal("103501"), MercadoPagoService.chargeAmount(order));
        order.setTotal(new BigDecimal("90000.00"));
        assertEquals(new BigDecimal("90000"), MercadoPagoService.chargeAmount(order));
    }
}
