package com.forehapp.store.trafficModule.application;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

/** Pure helpers: where a visit came from, which device, and what to ignore. */
public final class TrafficClassifier {

    private static final Pattern BOT = Pattern.compile(
            "bot|crawl|spider|slurp|headless|lighthouse|preview|facebookexternalhit|whatsapp/|monitor|curl|wget|python|java/",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLET = Pattern.compile("ipad|tablet|kindle|silk|playbook|(android(?!.*mobile))", Pattern.CASE_INSENSITIVE);
    private static final Pattern MOBILE = Pattern.compile("mobi|iphone|ipod|android|blackberry|opera mini|iemobile", Pattern.CASE_INSENSITIVE);

    private TrafficClassifier() {}

    public static boolean isBot(String userAgent) {
        return userAgent == null || userAgent.isBlank() || BOT.matcher(userAgent).find();
    }

    public static String device(String userAgent) {
        if (userAgent == null) return "desktop";
        if (TABLET.matcher(userAgent).find()) return "tablet";
        if (MOBILE.matcher(userAgent).find()) return "mobile";
        return "desktop";
    }

    /** Admin, seller and ambassador panels are internal traffic. */
    public static boolean isInternalPath(String path) {
        return path.startsWith("/admin") || path.startsWith("/seller") || path.startsWith("/ambassador");
    }

    /** Path without query or fragment, at most 255 chars; null if it does not look like a site path. */
    public static String cleanPath(String raw) {
        if (raw == null) return null;
        String p = raw.trim();
        int cut = indexOfAny(p, '?', '#');
        if (cut >= 0) p = p.substring(0, cut);
        if (!p.startsWith("/") || p.length() > 255) return null;
        return p;
    }

    public static String host(String referrer) {
        if (referrer == null || referrer.isBlank()) return null;
        try {
            String host = URI.create(referrer.trim()).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Source of a session: the utm_source when the link carries one, otherwise the referring site.
     * siteHost is our own domain (navigation inside the site counts as direct).
     */
    public static String source(String utmSource, String referrerHost, String siteHost) {
        if (utmSource != null && !utmSource.isBlank()) {
            return fromName(utmSource.toLowerCase(Locale.ROOT));
        }
        if (referrerHost == null || (siteHost != null && referrerHost.endsWith(siteHost))) return "direct";
        String fromHost = fromName(referrerHost);
        return "other".equals(fromHost) ? "referral" : fromHost;
    }

    private static String fromName(String s) {
        if (s.contains("google")) return "google";
        if (s.contains("instagram") || s.equals("ig")) return "instagram";
        if (s.contains("facebook") || s.equals("fb") || s.contains("fb.com") || s.contains("fb.me")) return "facebook";
        if (s.contains("whatsapp") || s.equals("wa") || s.contains("wa.me")) return "whatsapp";
        if (s.contains("tiktok")) return "tiktok";
        if (s.contains("bing") || s.contains("duckduckgo") || s.contains("yahoo") || s.contains("ecosia")) return "search";
        if (s.equals("t.co") || s.contains("twitter") || s.equals("x.com")) return "x";
        if (s.contains("youtube")) return "youtube";
        if (s.contains("mail") || s.contains("newsletter") || s.equals("email")) return "email";
        return "other";
    }

    private static int indexOfAny(String s, char a, char b) {
        int i = s.indexOf(a), j = s.indexOf(b);
        if (i < 0) return j;
        if (j < 0) return i;
        return Math.min(i, j);
    }
}
