package com.forehapp.store.paymentModule.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record MercadoPagoWebhookDto(
        String action,
        String type,
        Map<String, Object> data,
        @JsonProperty("live_mode") Boolean liveMode
) {}
