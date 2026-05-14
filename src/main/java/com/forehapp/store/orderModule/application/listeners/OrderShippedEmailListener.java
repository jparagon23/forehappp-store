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
        if (event.getNewStatus() != OrderSellerGroupStatus.SHIPPED) return;

        try {
            emailSender.sendEmail(
                    event.getBuyerEmail(),
                    "📦 Tu pedido está en camino — Orden #" + event.getOrderId(),
                    buildEmail(event)
            );
            log.info("[OrderShippedEmail] Sent shipped notification to buyer={} orderId={} tracking={}",
                    event.getBuyerEmail(), event.getOrderId(), event.getTrackingNumber());
        } catch (Exception e) {
            log.error("[OrderShippedEmail] Failed to send notification to buyer={} orderId={}",
                    event.getBuyerEmail(), event.getOrderId(), e);
        }
    }

    private String buildEmail(OrderStatusChangedEvent event) {
        StringBuilder itemRows = new StringBuilder();
        for (OrderStatusChangedEvent.ItemData item : event.getItems()) {
            itemRows.append("""
                    <tr>
                      <td style="padding:6px 0;font-size:13px;color:#222;">%s</td>
                      <td style="padding:6px 0;font-size:13px;color:#888;text-align:center;">%s</td>
                      <td style="padding:6px 0;font-size:13px;color:#222;text-align:center;">%d</td>
                    </tr>
                    """.formatted(item.productTitle(), item.sku(), item.quantity()));
        }

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
                          <p style="margin:4px 0 0;color:#a0a0c0;font-size:13px;">Tu pedido está en camino</p>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:32px;">

                          <div style="text-align:center;margin-bottom:24px;">
                            <div style="display:inline-block;width:64px;height:64px;background:#e3f2fd;border-radius:50%;line-height:64px;font-size:32px;">📦</div>
                          </div>

                          <h2 style="margin:0 0 8px;font-size:20px;color:#1565c0;text-align:center;">¡Tu pedido está en camino!</h2>
                          <p style="margin:0 0 24px;font-size:14px;color:#555;text-align:center;">
                            Hola <strong>%s</strong>, el vendedor ya despachó tu pedido.
                          </p>

                          <!-- Tracking -->
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

                          <!-- Items -->
                          <p style="margin:0 0 10px;font-size:13px;color:#888;font-weight:600;text-transform:uppercase;letter-spacing:.5px;">Productos enviados</p>
                          <table width="100%%" cellpadding="0" cellspacing="0" style="border-top:1px solid #eee;margin-bottom:24px;">
                            <thead>
                              <tr>
                                <th style="padding:8px 0;font-size:12px;color:#aaa;text-align:left;font-weight:600;">Producto</th>
                                <th style="padding:8px 0;font-size:12px;color:#aaa;text-align:center;font-weight:600;">SKU</th>
                                <th style="padding:8px 0;font-size:12px;color:#aaa;text-align:center;font-weight:600;">Cant.</th>
                              </tr>
                            </thead>
                            <tbody>
                              %s
                            </tbody>
                          </table>

                          <p style="margin:0;font-size:13px;color:#888;text-align:center;">
                            Puedes hacer seguimiento de tu envío con la guía indicada arriba.
                          </p>

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
                """.formatted(
                event.getBuyerName(),
                event.getTrackingNumber(),
                event.getOrderId(),
                event.getShippingCity(), event.getShippingCountry(),
                itemRows.toString()
        );
    }
}
