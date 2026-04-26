package com.forehapp.store.notificationModule.infrastructure.web;

import com.forehapp.store.notificationModule.application.dto.PushSubscriptionDto;
import com.forehapp.store.notificationModule.infrastructure.services.FcmNotificationService;
import com.forehapp.store.notificationModule.infrastructure.services.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;
    private final FcmNotificationService fcmNotificationService;

    @PostMapping("/subscribe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> subscribe(@AuthenticationPrincipal String userId,
                                          @RequestBody PushSubscriptionDto dto) {
        pushNotificationService.saveSubscription(Long.parseLong(userId), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unsubscribe(@AuthenticationPrincipal String userId,
                                            @RequestBody Map<String, String> body) {
        String endpoint = body.get("endpoint");
        pushNotificationService.unsubscribeByEndpoint(Long.parseLong(userId), endpoint);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/subscribe-fcm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> subscribeFcm(@AuthenticationPrincipal String userId,
                                             @RequestBody Map<String, String> body) {
        String token = body.get("token");
        fcmNotificationService.upsertToken(Long.parseLong(userId), token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe-fcm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unsubscribeFcm(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        fcmNotificationService.deactivateToken(token);
        return ResponseEntity.ok().build();
    }
}
