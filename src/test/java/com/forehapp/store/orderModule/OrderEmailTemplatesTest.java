package com.forehapp.store.orderModule;

import com.forehapp.store.mail.EmailSender;
import com.forehapp.store.orderModule.application.listeners.OrderEmailListener;
import com.forehapp.store.orderModule.application.listeners.OrderPaidEmailListener;
import com.forehapp.store.orderModule.domain.events.OrderCreatedEvent;
import com.forehapp.store.orderModule.domain.events.OrderPaidEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The templates use String.formatted: a stray % breaks the email at send time, so render them here. */
class OrderEmailTemplatesTest {

    private final List<String[]> sent = new ArrayList<>();
    private final EmailSender sender = (to, subject, html) -> {
        sent.add(new String[]{to, subject, html});
        return CompletableFuture.completedFuture(null);
    };

    @Test
    void paidEmailRenders() {
        new OrderPaidEmailListener(sender).onOrderPaid(
                new OrderPaidEvent(11L, "buyer@mail.com", "Ana Pérez", new BigDecimal("65000"), LocalDateTime.now()));

        assertEquals(1, sent.size());
        assertTrue(sent.get(0)[2].contains("#11"));
    }

    @Test
    void assistedGuestConfirmationShowsStoreNoticeAndAccountInvite() {
        OrderCreatedEvent event = event("TRANSFER", true).withBuyerContext(true, "Forehapp <Store>");

        new OrderEmailListener(sender, "https://forehapp.store/").onOrderCreated(event);

        String html = buyerEmail();
        assertTrue(html.contains("Pago recibido"));
        assertTrue(html.contains("Forehapp &lt;Store&gt;"), "store name is HTML-escaped");
        assertTrue(html.contains("https://forehapp.store/register?email=buyer%40mail.com"));
    }

    @Test
    void registeredBuyerGetsNoAccountInviteOrAssistedNotice() {
        new OrderEmailListener(sender, "https://forehapp.store").onOrderCreated(event("TRANSFER", false));

        String html = buyerEmail();
        assertTrue(html.contains("Pago por transferencia"));
        assertFalse(html.contains("Crear mi cuenta"));
        assertFalse(html.contains("lo registró"));
    }

    private OrderCreatedEvent event(String method, boolean paid) {
        OrderCreatedEvent.ItemData item = new OrderCreatedEvent.ItemData("Tubo de pelotas", "SKU-1", 2,
                new BigDecimal("30000"), new BigDecimal("60000"));
        OrderCreatedEvent.SellerGroupData group = new OrderCreatedEvent.SellerGroupData(
                List.of("seller@mail.com"), "Forehapp Store", new BigDecimal("60000"), new BigDecimal("5000"), List.of(item));
        return new OrderCreatedEvent(11L, "Ana Pérez", "buyer@mail.com", "Calle 1 # 2-3", "Cali", "Colombia",
                LocalDateTime.now(), new BigDecimal("65000"), method, List.of(group), paid);
    }

    private String buyerEmail() {
        return sent.stream().filter(s -> s[0].equals("buyer@mail.com")).findFirst().orElseThrow()[2];
    }
}
