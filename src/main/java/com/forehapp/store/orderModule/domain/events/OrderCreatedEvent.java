package com.forehapp.store.orderModule.domain.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderCreatedEvent {

    private final Long orderId;
    private final String buyerName;
    private final String buyerEmail;
    private final String shippingAddress;
    private final String shippingCity;
    private final String shippingCountry;
    private final LocalDateTime createdAt;
    private final BigDecimal total;
    private final String paymentMethod;
    private final List<SellerGroupData> sellerGroups;

    public OrderCreatedEvent(Long orderId,
                             String buyerName,
                             String buyerEmail,
                             String shippingAddress,
                             String shippingCity,
                             String shippingCountry,
                             LocalDateTime createdAt,
                             BigDecimal total,
                             String paymentMethod,
                             List<SellerGroupData> sellerGroups) {
        this.orderId = orderId;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.shippingAddress = shippingAddress;
        this.shippingCity = shippingCity;
        this.shippingCountry = shippingCountry;
        this.createdAt = createdAt;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.sellerGroups = sellerGroups;
    }

    public Long getOrderId()             { return orderId; }
    public String getBuyerName()         { return buyerName; }
    public String getBuyerEmail()        { return buyerEmail; }
    public String getShippingAddress()   { return shippingAddress; }
    public String getShippingCity()      { return shippingCity; }
    public String getShippingCountry()   { return shippingCountry; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public BigDecimal getTotal()         { return total; }
    public String getPaymentMethod()     { return paymentMethod; }
    public List<SellerGroupData> getSellerGroups() { return sellerGroups; }

    public record SellerGroupData(
            List<String> memberEmails,
            String storeName,
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
