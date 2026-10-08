package com.forehapp.store.security.filter;

/** Test access to the package-private client IP rule. */
public final class RateLimitFilterAccess {
    private RateLimitFilterAccess() {}

    public static String clientIp(String forwardedFor, String remoteAddr, int hops) {
        return RateLimitFilter.clientIp(forwardedFor, remoteAddr, hops);
    }
}
