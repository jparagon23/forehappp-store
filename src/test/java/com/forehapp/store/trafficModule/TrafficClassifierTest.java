package com.forehapp.store.trafficModule;

import com.forehapp.store.trafficModule.application.TrafficClassifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrafficClassifierTest {

    private static final String SITE = "forehappstore.com";

    @Test
    void sourceFromReferrerOrUtm() {
        assertEquals("google", TrafficClassifier.source(null, "google.com.co", SITE));
        assertEquals("instagram", TrafficClassifier.source(null, "l.instagram.com", SITE));
        assertEquals("facebook", TrafficClassifier.source(null, "m.facebook.com", SITE));
        assertEquals("whatsapp", TrafficClassifier.source(null, "wa.me", SITE));
        assertEquals("direct", TrafficClassifier.source(null, null, SITE));
        assertEquals("direct", TrafficClassifier.source(null, "forehappstore.com", SITE));
        assertEquals("referral", TrafficClassifier.source(null, "blogdetenis.co", SITE));
        // A tagged link wins over the referrer
        assertEquals("instagram", TrafficClassifier.source("ig", "google.com", SITE));
        assertEquals("email", TrafficClassifier.source("newsletter", null, SITE));
    }

    @Test
    void deviceAndBots() {
        assertEquals("mobile", TrafficClassifier.device("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) Mobile/15E148"));
        assertEquals("mobile", TrafficClassifier.device("Mozilla/5.0 (Linux; Android 14; SM-A546E) Chrome/120 Mobile Safari/537.36"));
        assertEquals("tablet", TrafficClassifier.device("Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X)"));
        assertEquals("desktop", TrafficClassifier.device("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120"));
        assertTrue(TrafficClassifier.isBot("Mozilla/5.0 (compatible; Googlebot/2.1)"));
        assertTrue(TrafficClassifier.isBot("facebookexternalhit/1.1"));
        assertTrue(TrafficClassifier.isBot(null));
        assertFalse(TrafficClassifier.isBot("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120"));
    }

    @Test
    void pathsAndHosts() {
        assertEquals("/product/12", TrafficClassifier.cleanPath("/product/12?utm_source=ig#reviews"));
        assertNull(TrafficClassifier.cleanPath("https://evil.com/x"));
        assertTrue(TrafficClassifier.isInternalPath("/admin/dashboard"));
        assertTrue(TrafficClassifier.isInternalPath("/seller/orders"));
        assertFalse(TrafficClassifier.isInternalPath("/product/1"));
        assertEquals("google.com", TrafficClassifier.host("https://www.google.com/search?q=raqueta"));
        assertNull(TrafficClassifier.host("not a url"));
    }
}
