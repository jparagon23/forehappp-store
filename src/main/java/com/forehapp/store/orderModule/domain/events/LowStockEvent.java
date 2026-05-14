package com.forehapp.store.orderModule.domain.events;

public class LowStockEvent {

    private final String sellerEmail;
    private final String sellerName;
    private final String productTitle;
    private final String sku;
    private final int currentStock;

    public LowStockEvent(String sellerEmail, String sellerName,
                         String productTitle, String sku, int currentStock) {
        this.sellerEmail = sellerEmail;
        this.sellerName = sellerName;
        this.productTitle = productTitle;
        this.sku = sku;
        this.currentStock = currentStock;
    }

    public String getSellerEmail()  { return sellerEmail; }
    public String getSellerName()   { return sellerName; }
    public String getProductTitle() { return productTitle; }
    public String getSku()          { return sku; }
    public int getCurrentStock()    { return currentStock; }
    public boolean isOutOfStock()   { return currentStock == 0; }
}
