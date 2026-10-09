package com.forehapp.store.general.constants;

import java.util.Set;

public class Constants {
    // users.user_status is shared with the ForehApp (appointments) app, which also writes it
    public static final int PENDING_STATUS       = 1;
    public static final int ACTIVE_USER_STATUS   = 2;
    /** Created by a ForehApp organizer; the person never finished signing up but can log in with Google. */
    public static final int PRE_REGISTER_STATUS  = 7;
    private static final Set<Integer> STORE_SESSION_STATUSES = Set.of(ACTIVE_USER_STATUS, PRE_REGISTER_STATUS);

    /** Whether a user with this status may log in to the store with Google and keep the session. */
    public static boolean canHoldStoreSession(Integer userStatus) {
        return userStatus != null && STORE_SESSION_STATUSES.contains(userStatus);
    }

    public static final int USER_ROLE_ID         = 1;
}
