package com.forehapp.store.trafficModule.infrastructure.web;

import com.forehapp.store.trafficModule.application.TrafficReportResponse;
import com.forehapp.store.trafficModule.application.TrafficReportService;
import com.forehapp.store.trafficModule.application.TrafficTrackingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrafficController {

    private final TrafficTrackingService tracking;
    private final TrafficReportService reports;

    public TrafficController(TrafficTrackingService tracking, TrafficReportService reports) {
        this.tracking = tracking;
        this.reports = reports;
    }

    /**
     * Public, anonymous. The storefront sends text/plain JSON (no CORS preflight) with keepalive,
     * so events survive page unloads. Always 204, even for dropped events.
     */
    @PostMapping("/api/v1/track")
    public ResponseEntity<Void> track(@RequestBody(required = false) String body,
                                      @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        tracking.track(body, userAgent);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/admin/reports/traffic")
    public ResponseEntity<TrafficReportResponse> report(@AuthenticationPrincipal String userId,
                                                        @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reports.report(Long.parseLong(userId), days));
    }
}
