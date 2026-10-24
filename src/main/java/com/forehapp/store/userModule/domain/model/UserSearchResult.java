package com.forehapp.store.userModule.domain.model;

import java.time.LocalDateTime;

/**
 * A user found by the admin search. The users table is shared with ForehApp, so
 * hasStoreAccount tells whether the person has ever used the store.
 */
public record UserSearchResult(
        Long id,
        String name,
        String lastname,
        String email,
        String phone,
        LocalDateTime creationDate,
        boolean hasStoreAccount
) {}
