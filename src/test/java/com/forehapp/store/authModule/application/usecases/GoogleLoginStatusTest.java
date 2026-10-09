package com.forehapp.store.authModule.application.usecases;

import com.forehapp.store.authModule.application.services.AuthSessionService;
import com.forehapp.store.authModule.application.services.GoogleAuthService;
import com.forehapp.store.general.constants.Constants;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.orderModule.domain.ports.in.IGuestOrderLinkService;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Store Google login against statuses written by ForehApp (shared users table). */
class GoogleLoginStatusTest {

    private final User user = new User();
    private final AuthSessionService sessions = mock(AuthSessionService.class);
    private AuthUseCasesImpl auth;

    @BeforeEach
    void setUp() {
        user.setId(9L);
        user.setEmail("player@example.com");
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("player@example.com");
        GoogleAuthService google = mock(GoogleAuthService.class);
        when(google.verifyToken("id-token")).thenReturn(payload);

        UserRepository users = mock(UserRepository.class);
        when(users.findByEmail("player@example.com")).thenReturn(Optional.of(user));
        IStoreProfileDao profiles = mock(IStoreProfileDao.class);
        when(profiles.findByUserId(9L)).thenReturn(Optional.of(new StoreProfile()));
        when(sessions.open(any())).thenReturn(new AuthSessionService.Tokens("a", "r"));

        auth = new AuthUseCasesImpl(users, null, null, null, null, profiles, google,
                mock(IGuestOrderLinkService.class), sessions);
    }

    @Test
    void activeAndPreRegisteredUsersCanLogIn() {
        user.setUserStatus(Constants.ACTIVE_USER_STATUS);
        assertEquals("a", auth.loginWithGoogle("id-token").getAccess_token());

        user.setUserStatus(Constants.PRE_REGISTER_STATUS);
        assertEquals("a", auth.loginWithGoogle("id-token").getAccess_token());
        assertEquals(Constants.PRE_REGISTER_STATUS, user.getUserStatus(), "ForehApp still needs to finish the sign-up");
    }

    @Test
    void accountDeletedInForehappCannotLogIn() {
        user.setUserStatus(5);
        BadRequestException e = assertThrows(BadRequestException.class, () -> auth.loginWithGoogle("id-token"));
        assertEquals(ErrorCode.AUTH_ACCOUNT_DISABLED, e.getCode());
        verify(sessions, never()).open(any());
    }
}
