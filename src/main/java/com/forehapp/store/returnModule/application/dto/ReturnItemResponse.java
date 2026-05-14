package com.forehapp.store.returnModule.application.dto;

import java.math.BigDecimal;

public record ReturnItemResponse(
        Long orderItemId,
        String productTitle,
        String variantSku,
        Integer quantityOrdered,
        Integer quantityToReturn,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
