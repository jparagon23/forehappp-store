package com.forehapp.store.notificationModule.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PushSubscriptionDto {
    private String endpoint;
    private Long expirationTime;
    private Keys keys;

    @Getter @Setter
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
