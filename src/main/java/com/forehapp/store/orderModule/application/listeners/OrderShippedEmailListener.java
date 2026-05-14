package com.forehapp.store.orderModule.application.listeners;

import com.forehapp.store.mail.EmailSender;
import com.forehapp.store.orderModule.domain.events.OrderStatusChangedEvent;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderShippedEmailListener {

    private static final Logger log = LoggerFactory.getLogger(OrderShippedEmailListener.class);

    private final EmailSender emailSender;

    public OrderShippedEmailListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        String subject = buildSubject(event);
        if (subject == null) return;

        try {
            emailSender.sendEmail(event.getBuyerEmail(), subject, buildEmail(event));
            log.info("[OrderStatusEmail] status={} buyer={} orderId={}",
                    event.getNewStatus(), event.getBuyerEmail(), event.getOrderId());
        } catch (Exception e) {
            log.error("[OrderStatusEmail] Failed status={} buyer={} orderId={}",
                    event.getNewStatus(), event.getBuyerEmail(), event.getOrderId(), e);
        }
    }

    private String buildSubject(OrderStatusChangedEvent event) {
        return switch (event.getNewStatus()) {
            case PREPARING -> "🔧 Tu pedido está siendo preparado — Orden #" + event.getOrderId();
            case SHIPPED   -> "📦 Tu pedido está en camino — Orden #" + event.getOrderId();
            case DELIVERED -> "✅ Tu pedido fue entregado — Orden #" + event.getOrderId();
            case CANCELLED -> "❌ Tu pedido fue cancelado — Orden #" + event.getOrderId();
            default        -> null;
        };
    }

    private String buildEmail(OrderStatusChangedEvent event) {
        return switch (event.getNewStatus()) {
            case PREPARING -> buildPreparingEmail(event);
            case SHIPPED   -> buildShippedEmail(event);
            case DELIVERED -> buildDeliveredEmail(event);
            case CANCELLED -> buildCancelledEmail(event);
            default        -> "";
        };
    }

    // ── email builders ─────────────────────────────────────────────────────────

    private String buildPreparingEmail(OrderStatusChangedEvent event) {
        return wrapLayout("🔧", "#e3f2fd", "#1565c0",
                "Tu pedido está siendo preparado",
                "Hola <strong>%s</strong>, el vendedor ya está alistando tu pedido.".formatted(event.getBuyerName()),
                """
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#e8f5e9;border-left:4px solid #2e7d32;border-radius:4px;padding:16px 20px;margin-bottom:24px;">
                  <tr>
                    <td style="font-size:13px;color:#555;width:130px;">Orden #</td>
                    <td style="font-size:16px;color:#1b5e20;font-weight:700;">#%d</td>
                  </tr>
                  <tr>
                    <td style="padding-top:6px;font-size:13px;color:#555;">Destino</td>
                    <td style="padding-top:6px;font-size:13px;color:#222;">%s, %s</td>
                  </tr>
                </table>
                """.formatted(event.getOrderId(), event.getShippingCity(), event.getShippingCountry())
                        + buildItemTable(event),
                "Pronto recibirás un nuevo aviso cuando tu pedido sea despachado.");
    }

    private String buildShippedEmail(OrderStatusChangedEvent event) {
        return wrapLayout("📦", "#e3f2fd", "#1565c0",
                "¡Tu pedido está en camino!",
                "Hola <strong>%s</strong>, el vendedor ya despachó tu pedido.".formatted(event.getBuyerName()),
                """
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#e8f5e9;border-left:4px solid #2e7d32;border-radius:4px;padding:16px 20px;margin-bottom:24px;">
                  <tr>
                    <td style="font-size:13px;color:#555;width:130px;">Guía de envío</td>
                    <td style="font-size:16px;color:#1b5e20;font-weight:700;letter-spacing:1px;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding-top:6px;font-size:13px;color:#555;">Orden #</td>
                    <td style="padding-top:6px;font-size:13px;color:#222;">#%d</td>
                  </tr>
                  <tr>
                    <td style="padding-top:6px;font-size:13px;color:#555;">Destino</td>
                    <td style="padding-top:6px;font-size:13px;color:#222;">%s, %s</td>
                  </tr>
                </table>
                """.formatted(event.getTrackingNumber(), event.getOrderId(),
                        event.getShippingCity(), event.getShippingCountry())
                        + buildItemTable(event),
                "Puedes hacer seguimiento de tu envío con la guía indicada arriba.");
    }

    private String buildDeliveredEmail(OrderStatusChangedEvent event) {
        return wrapLayout("✅", "#e8f5e9", "#2e7d32",
                "¡Tu pedido fue entregado!",
                "Hola <strong>%s</strong>, el vendedor confirmó la entrega de tu pedido.".formatted(event.getBuyerName()),
                """
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#e8f5e9;border-left:4px solid #2e7d32;border-radius:4px;padding:16px 20px;margin-bottom:24px;">
                  <tr>
                    <td style="font-size:13px;color:#555;width:130px;">Orden #</td>
                    <td style="font-size:16px;color:#1b5e20;font-weight:700;">#%d</td>
                  </tr>
                </table>
                """.formatted(event.getOrderId())
                        + buildItemTable(event),
                "Esperamos que hayas disfrutado tu compra. ¡Gracias por usar Forehapp!");
    }

    private String buildCancelledEmail(OrderStatusChangedEvent event) {
        return wrapLayout("❌", "#fce4ec", "#b71c1c",
                "Tu pedido fue cancelado",
                "Hola <strong>%s</strong>, lamentamos informarte que el vendedor canceló tu pedido.".formatted(event.getBuyerName()),
                """
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#ffebee;border-left:4px solid #c62828;border-radius:4px;padding:16px 20px;margin-bottom:24px;">
                  <tr>
                    <td style="font-size:13px;color:#555;width:130px;">Orden #</td>
                    <td style="font-size:16px;color:#b71c1c;font-weight:700;">#%d</td>
                  </tr>
                  <tr>
                    <td style="padding-top:8px;font-size:13px;color:#555;vertical-align:top;">Motivo</td>
                    <td style="padding-top:8px;font-size:14px;color:#c62828;font-weight:600;">%s</td>
                  </tr>
                </table>
                """.formatted(event.getOrderId(), event.getCancellationReason())
                        + buildItemTable(event),
                "Si tienes dudas, por favor contáctanos a través de la plataforma.");
    }

    // ── shared helpers ─────────────────────────────────────────────────────────

    private String buildItemTable(OrderStatusChangedEvent event) {
        StringBuilder rows = new StringBuilder();
        for (OrderStatusChangedEvent.ItemData item : event.getItems()) {
            rows.append("""
                    <tr>
                      <td style="padding:6px 0;font-size:13px;color:#222;">%s</td>
                      <td style="padding:6px 0;font-size:13px;color:#888;text-align:center;">%s</td>
                      <td style="padding:6px 0;font-size:13px;color:#222;text-align:center;">%d</td>
                    </tr>
                    """.formatted(item.productTitle(), item.sku(), item.quantity()));
        }
        return """
                <p style="margin:0 0 10px;font-size:13px;color:#888;font-weight:600;text-transform:uppercase;letter-spacing:.5px;">Productos</p>
                <table width="100%%" cellpadding="0" cellspacing="0" style="border-top:1px solid #eee;margin-bottom:24px;">
                  <thead>
                    <tr>
                      <th style="padding:8px 0;font-size:12px;color:#aaa;text-align:left;font-weight:600;">Producto</th>
                      <th style="padding:8px 0;font-size:12px;color:#aaa;text-align:center;font-weight:600;">SKU</th>
                      <th style="padding:8px 0;font-size:12px;color:#aaa;text-align:center;font-weight:600;">Cant.</th>
                    </tr>
                  </thead>
                  <tbody>%s</tbody>
                </table>
                """.formatted(rows.toString());
    }

    private String wrapLayout(String icon, String iconBg, String titleColor,
                               String title, String subtitle, String body, String footer) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:32px 0;">
                  <tr><td align="center">
                    <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                      <tr>
                        <td style="background:#1a1a2e;padding:24px 32px;">
                          <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:700;">Forehapp Store</h1>
                          <p style="margin:4px 0 0;color:#a0a0c0;font-size:13px;">Actualización de tu pedido</p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:32px;">
                          <div style="text-align:center;margin-bottom:24px;">
                            <div style="display:inline-block;width:64px;height:64px;background:%s;border-radius:50%%;line-height:64px;font-size:32px;">%s</div>
                          </div>
                          <h2 style="margin:0 0 8px;font-size:20px;color:%s;text-align:center;">%s</h2>
                          <p style="margin:0 0 24px;font-size:14px;color:#555;text-align:center;">%s</p>
                          %s
                          <p style="margin:0;font-size:13px;color:#888;text-align:center;">%s</p>
                        </td>
                      </tr>
                      <tr>
                        <td style="background:#f8f8f8;padding:18px 32px;text-align:center;">
                          <p style="margin:0;font-size:12px;color:#aaa;">© 2026 Forehapp · Todos los derechos reservados</p>
                        </td>
                      </tr>
                    </table>
                  </td></tr>
                </table>
                </body>
                </html>
                """.formatted(iconBg, icon, titleColor, title, subtitle, body, footer);
    }
}
