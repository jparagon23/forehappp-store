package com.forehapp.store.paymentModule.application.usecases;

import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MercadoPagoPreferenceTest {

    @Test
    void linkChargesTheOrderTotalWithShippingCouponAndSurcharge() {
        // 2 x 40.000 + 1 x 20.000 = 100.000; shipping 12.000; coupon -10.000; 3.5% surcharge 3.570
        Order order = new Order();
        order.setId(55L);
        order.setTotal(new BigDecimal("105570.00"));
        OrderSellerGroup group = new OrderSellerGroup();
        group.setShippingCost(new BigDecimal("12000"));
        group.getItems().add(item("Grip Wilson", 2, "40000"));
        group.getItems().add(item("Tubo de pelotas", 1, "20000"));
        order.getSellerGroups().add(group);

        MercadoPagoService service = new MercadoPagoService(null);
        ReflectionTestUtils.setField(service, "currency", "COP");
        ReflectionTestUtils.setField(service, "successUrl", "https://example.com/ok");
        ReflectionTestUtils.setField(service, "failureUrl", "https://example.com/fail");
        ReflectionTestUtils.setField(service, "pendingUrl", "https://example.com/pending");

        PreferenceRequest request = service.preferenceRequest(order);

        assertEquals(1, request.getItems().size());
        PreferenceItemRequest line = request.getItems().get(0);
        assertEquals(1, line.getQuantity());
        assertEquals(new BigDecimal("105570"), line.getUnitPrice(), "charges the order total, not the products");
        assertEquals("COP", line.getCurrencyId());
        assertTrue(line.getDescription().contains("2 x Grip Wilson"));
        assertEquals("55", request.getExternalReference());
    }

    private static OrderItem item(String title, int quantity, String unitPrice) {
        Product product = new Product();
        product.setTitle(title);
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        OrderItem item = new OrderItem();
        item.setVariant(variant);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        return item;
    }
}
