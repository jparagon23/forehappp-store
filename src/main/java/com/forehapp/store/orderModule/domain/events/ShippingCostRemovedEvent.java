package com.forehapp.store.orderModule.domain.events;

import java.math.BigDecimal;

public class ShippingCostRemovedEvent {

    private final Long groupId;
    private final Long orderId;
    private final String buyerEmail;
    private final String buyerName;
    private final String storeName;
    private final BigDecimal waivedAmount;
    private final BigDecimal newOrderTotal;
    private final String reason;

    public ShippingCostRemovedEvent(Long groupId, Long orderId, String buyerEmail, String buyerName,
                                     String storeName, BigDecimal waivedAmount, BigDecimal newOrderTotal,
                                     String reason) {
        this.groupId = groupId;
        this.orderId = orderId;
        this.buyerEmail = buyerEmail;
        this.buyerName = buyerName;
        this.storeName = storeName;
        this.waivedAmount = waivedAmount;
        this.newOrderTotal = newOrderTotal;
        this.reason = reason;
    }

    public Long getGroupId()             { return groupId; }
    public Long getOrderId()             { return orderId; }
    public String getBuyerEmail()        { return buyerEmail; }
    public String getBuyerName()         { return buyerName; }
    public String getStoreName()         { return storeName; }
    public BigDecimal getWaivedAmount()   { return waivedAmount; }
    public BigDecimal getNewOrderTotal()  { return newOrderTotal; }
    public String getReason()            { return reason; }
}
