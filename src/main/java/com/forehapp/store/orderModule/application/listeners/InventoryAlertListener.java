package com.forehapp.store.orderModule.application.listeners;

import com.forehapp.store.mail.EmailSender;
import com.forehapp.store.orderModule.domain.events.LowStockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InventoryAlertListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryAlertListener.class);

    private final EmailSender emailSender;

    public InventoryAlertListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStock(LowStockEvent event) {
        try {
            String subject = event.isOutOfStock()
                    ? "⚠️ Sin stock: " + event.getProductTitle()
                    : "⚠️ Stock bajo: " + event.getProductTitle();

            emailSender.sendEmail(event.getSellerEmail(), subject, buildEmail(event));
            log.info("[InventoryAlert] Sent {} alert to seller={} sku={} stock={}",
                    event.isOutOfStock() ? "OUT_OF_STOCK" : "LOW_STOCK",
                    event.getSellerEmail(), event.getSku(), event.getCurrentStock());
        } catch (Exception e) {
            log.error("[InventoryAlert] Failed to send alert to seller={} sku={}",
                    event.getSellerEmail(), event.getSku(), e);
        }
    }

    private String buildEmail(LowStockEvent event) {
        String badgeColor  = event.isOutOfStock() ? "#e53935" : "#f57c00";
        String badgeText   = event.isOutOfStock() ? "SIN STOCK" : "STOCK BAJO";
        String headline    = event.isOutOfStock()
                ? "Tu producto <strong>" + event.getProductTitle() + "</strong> se quedó sin stock."
                : "Tu producto <strong>" + event.getProductTitle() + "</strong> tiene solo <strong>"
                  + event.getCurrentStock() + " unidad(es)</strong> disponible(s).";
        String advice      = event.isOutOfStock()
                ? "El producto ya no aparecerá disponible para los compradores hasta que actualices el inventario."
                : "Te recomendamos reabastecer pronto para no perder ventas.";

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
                          <p style="margin:4px 0 0;color:#a0a0c0;font-size:13px;">Alerta de inventario</p>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:32px;">
                          <p style="margin:0 0 16px;font-size:15px;color:#333;">Hola, <strong>%s</strong></p>

                          <div style="background:#fff8f0;border-left:4px solid %s;border-radius:4px;padding:16px 20px;margin-bottom:24px;">
                            <span style="display:inline-block;background:%s;color:#fff;font-size:11px;font-weight:700;padding:2px 8px;border-radius:3px;margin-bottom:10px;">%s</span>
                            <p style="margin:0;font-size:14px;color:#333;">%s</p>
                          </div>

                          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f8f8f8;border-radius:6px;padding:14px 16px;margin-bottom:20px;">
                            <tr>
                              <td style="padding:3px 0;font-size:13px;color:#888;width:120px;">Producto</td>
                              <td style="padding:3px 0;font-size:13px;color:#222;font-weight:600;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:3px 0;font-size:13px;color:#888;">SKU</td>
                              <td style="padding:3px 0;font-size:13px;color:#222;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:3px 0;font-size:13px;color:#888;">Stock actual</td>
                              <td style="padding:3px 0;font-size:13px;color:%s;font-weight:700;">%d unidad(es)</td>
                            </tr>
                          </table>

                          <p style="margin:0;font-size:13px;color:#666;">%s</p>
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
                event.getSellerName(),
                badgeColor, badgeColor, badgeText,
                headline,
                event.getProductTitle(),
                event.getSku(),
                event.isOutOfStock() ? "#e53935" : "#f57c00",
                event.getCurrentStock(),
                advice
        );
    }
}
