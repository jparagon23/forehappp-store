package com.forehapp.store.orderModule.domain.events;

import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;

import java.util.List;

public class OrderStatusChangedEvent {

    private final Long groupId;
    private final Long orderId;
    private final String buyerEmail;
    private final String buyerName;
    private final OrderSellerGroupStatus newStatus;
    private final String trackingNumber;
    private final String cancellationReason;
    private final String shippingAddress;
    private final String shippingCity;
    private final String shippingCountry;
    private final List<ItemData> items;

    public OrderStatusChangedEvent(Long groupId, Long orderId, String buyerEmail, String buyerName,
                                   OrderSellerGroupStatus newStatus, String trackingNumber,
                                   String cancellationReason,
                                   String shippingAddress, String shippingCity, String shippingCountry,
                                   List<ItemData> items) {
        this.groupId = groupId;
        this.orderId = orderId;
        this.buyerEmail = buyerEmail;
        this.buyerName = buyerName;
        this.newStatus = newStatus;
        this.trackingNumber = trackingNumber;
        this.cancellationReason = cancellationReason;
        this.shippingAddress = shippingAddress;
        this.shippingCity = shippingCity;
        this.shippingCountry = shippingCountry;
        this.items = items;
    }

    public Long getGroupId()            { return groupId; }
    public Long getOrderId()            { return orderId; }
    public String getBuyerEmail()       { return buyerEmail; }
    public String getBuyerName()        { return buyerName; }
    public OrderSellerGroupStatus getNewStatus() { return newStatus; }
    public String getTrackingNumber()   { return trackingNumber; }
    public String getCancellationReason() { return cancellationReason; }
    public String getShippingAddress()  { return shippingAddress; }
    public String getShippingCity()     { return shippingCity; }
    public String getShippingCountry()  { return shippingCountry; }
    public List<ItemData> getItems()    { return items; }

    public record ItemData(String productTitle, String sku, int quantity) {}
}
