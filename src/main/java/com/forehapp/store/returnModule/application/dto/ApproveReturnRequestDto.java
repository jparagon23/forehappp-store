package com.forehapp.store.returnModule.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ApproveReturnRequestDto(
        @NotNull @DecimalMin("0.01") BigDecimal refundAmount,
        @Size(max = 1000) String adminNotes
) {}
