package com.forehapp.store.orderModule;

import com.forehapp.store.orderModule.application.listeners.OrderItemsChangedEmailListener;
import com.forehapp.store.orderModule.domain.events.OrderItemsChangedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderItemsChangedEmailTest {

    private final OrderItemsChangedEmailListener listener = new OrderItemsChangedEmailListener(null);

    private static OrderItemsChangedEvent event(boolean pending, BigDecimal balance, String url) {
        return new OrderItemsChangedEvent(42L, "a@b.com", "Ana <b>", "Forehapp store",
                "Sin stock del sabor, 100% acordado con el cliente",
                List.of(new OrderItemsChangedEvent.Line("REPLACED", "Whey Chocolate", 1, new BigDecimal("120000"),
                                "Whey Vainilla", 1, new BigDecimal("125000")),
                        new OrderItemsChangedEvent.Line("REMOVED", "Shaker", 1, new BigDecimal("15000"), null, null, null)),
                new BigDecimal("135000"), new BigDecimal("125000"), pending, balance, url);
    }

    @Test
    void showsChangesAndEscapesTextFromSellers() {
        String html = listener.buildEmail(event(true, null, null));
        assertTrue(html.contains("Reemplazado"));
        assertTrue(html.contains("Retirado"));
        assertTrue(html.contains("100% acordado"));
        assertTrue(html.contains("Ana &lt;b&gt;"));
        assertFalse(html.contains("<b>Ana"));
        assertTrue(html.contains("valor a pagar ahora"));
    }

    @Test
    void paidOrderShowsDifferenceOrCredit() {
        assertTrue(listener.buildEmail(event(false, new BigDecimal("5000"), null)).contains("diferencia por pagar"));
        assertTrue(listener.buildEmail(event(false, new BigDecimal("-10000"), null)).contains("saldo a favor"));
        assertTrue(listener.buildEmail(event(false, null, null)).contains("no tienes que hacer nada"));
    }

    @Test
    void unpaidMercadoPagoOrderGetsTheNewLink() {
        String html = listener.buildEmail(event(true, null, "https://mp.example/checkout?pref=1&x=2"));
        assertTrue(html.contains("https://mp.example/checkout?pref=1&amp;x=2"));
        assertTrue(html.contains("nuevo link"));
    }
}
