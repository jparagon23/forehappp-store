package com.forehapp.store.orderModule.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderPaidEvent {

    private final Long orderId;
    private final String buyerEmail;
    private final String buyerName;
    private final BigDecimal total;
    private final LocalDateTime createdAt;

    public OrderPaidEvent(Long orderId, String buyerEmail, String buyerName,
                          BigDecimal total, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.buyerEmail = buyerEmail;
        this.buyerName = buyerName;
        this.total = total;
        this.createdAt = createdAt;
    }

    public Long getOrderId()       { return orderId; }
    public String getBuyerEmail()  { return buyerEmail; }
    public String getBuyerName()   { return buyerName; }
    public BigDecimal getTotal()   { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
