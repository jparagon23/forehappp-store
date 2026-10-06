package com.forehapp.store.orderModule.infrastructure.web.dto;

/**
 * Whether an email already has an active account (the order will go straight to it).
 * displayName is shortened ("Juan P.") so a seller cannot read customers' data by guessing emails.
 */
public record AssistedCustomerResponse(boolean registered, String displayName) {}
