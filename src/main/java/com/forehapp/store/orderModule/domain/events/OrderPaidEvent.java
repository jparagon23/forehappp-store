package com.forehapp.store.orderModule.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderPaidEvent {

    private final Long orderId;
    private final String buyerEmail;
    private final String buyerName;
    private final BigDecimal total;
    private final LocalDateTime createdAt;
    private boolean guestBuyer;
    private String registeredByStore;

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
    public boolean isGuestBuyer()        { return guestBuyer; }
    public String getRegisteredByStore() { return registeredByStore; }

    /**
     * guestBuyer: the order has no account behind it (the email invites to create one).
     * registeredByStore: name of the store that registered an ASSISTED order for the buyer; null otherwise.
     */
    public OrderPaidEvent withBuyerContext(boolean guestBuyer, String registeredByStore) {
        this.guestBuyer = guestBuyer;
        this.registeredByStore = registeredByStore;
        return this;
    }
}
