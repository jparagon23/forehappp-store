package com.forehapp.store.orderModule.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderCreatedEvent {

    private final Long orderId;
    private final String buyerName;
    private final String shippingAddress;
    private final String shippingCity;
    private final String shippingCountry;
    private final LocalDateTime createdAt;
    private final List<SellerGroupData> sellerGroups;

    public OrderCreatedEvent(Long orderId,
                             String buyerName,
                             String shippingAddress,
                             String shippingCity,
                             String shippingCountry,
                             LocalDateTime createdAt,
                             List<SellerGroupData> sellerGroups) {
        this.orderId = orderId;
        this.buyerName = buyerName;
        this.shippingAddress = shippingAddress;
        this.shippingCity = shippingCity;
        this.shippingCountry = shippingCountry;
        this.createdAt = createdAt;
        this.sellerGroups = sellerGroups;
    }

    public Long getOrderId() { return orderId; }
    public String getBuyerName() { return buyerName; }
    public String getShippingAddress() { return shippingAddress; }
    public String getShippingCity() { return shippingCity; }
    public String getShippingCountry() { return shippingCountry; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<SellerGroupData> getSellerGroups() { return sellerGroups; }

    public record SellerGroupData(
            String sellerEmail,
            String sellerName,
            BigDecimal subtotal,
            List<ItemData> items
    ) {}

    public record ItemData(
            String productTitle,
            String sku,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}
}
