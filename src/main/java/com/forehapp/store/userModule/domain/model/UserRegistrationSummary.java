package com.forehapp.store.userModule.domain.model;

import java.time.LocalDateTime;

public record UserRegistrationSummary(
        Long id,
        String name,
        String lastname,
        String email,
        String phone,
        LocalDateTime creationDate
) {}
