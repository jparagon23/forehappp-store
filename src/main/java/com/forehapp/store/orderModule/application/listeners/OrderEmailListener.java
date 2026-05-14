package com.forehapp.store.orderModule.application.listeners;

import com.forehapp.store.mail.EmailSender;
import com.forehapp.store.orderModule.domain.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class OrderEmailListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEmailListener.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getNumberInstance(new Locale("es", "CO"));

    private final EmailSender emailSender;

    public OrderEmailListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        for (OrderCreatedEvent.SellerGroupData group : event.getSellerGroups()) {
            try {
                String html = buildSellerEmail(event, group);
                emailSender.sendEmail(
                        group.sellerEmail(),
                        "Nueva orden #" + event.getOrderId() + " recibida en Forehapp",
                        html
                );
                log.info("[OrderEmail] Sent new order email to seller={} orderId={}", group.sellerEmail(), event.getOrderId());
            } catch (Exception e) {
                log.error("[OrderEmail] Failed to send email to seller={} orderId={}", group.sellerEmail(), event.getOrderId(), e);
            }
        }
    }

    private String buildSellerEmail(OrderCreatedEvent event, OrderCreatedEvent.SellerGroupData group) {
        StringBuilder rows = new StringBuilder();
        for (OrderCreatedEvent.ItemData item : group.items()) {
            rows.append("""
                    <tr>
                      <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;">%s<br><span style="color:#888;font-size:12px;">SKU: %s</span></td>
                      <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;text-align:center;">%d</td>
                      <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;text-align:right;">$%s</td>
                      <td style="padding:10px 8px;border-bottom:1px solid #f0f0f0;text-align:right;font-weight:600;">$%s</td>
                    </tr>
                    """.formatted(
                    item.productTitle(),
                    item.sku(),
                    item.quantity(),
                    formatAmount(item.unitPrice()),
                    formatAmount(item.subtotal())
            ));
        }

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:32px 0;">
                  <tr><td align="center">
                    <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                      <!-- Header -->
                      <tr>
                        <td style="background:#1a1a2e;padding:28px 32px;">
                          <h1 style="margin:0;color:#ffffff;font-size:22px;font-weight:700;">Forehapp Store</h1>
                          <p style="margin:6px 0 0;color:#a0a0c0;font-size:14px;">Notificación de nueva orden</p>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:32px;">

                          <p style="margin:0 0 8px;font-size:16px;color:#333;">Hola, <strong>%s</strong></p>
                          <p style="margin:0 0 24px;font-size:14px;color:#555;">
                            Tienes una nueva orden que requiere tu atención.
                          </p>

                          <!-- Order info -->
                          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f8f8f8;border-radius:6px;padding:16px;margin-bottom:24px;">
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;width:140px;">Número de orden</td>
                              <td style="padding:4px 0;font-size:13px;color:#222;font-weight:600;">#%d</td>
                            </tr>
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;">Fecha</td>
                              <td style="padding:4px 0;font-size:13px;color:#222;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;">Comprador</td>
                              <td style="padding:4px 0;font-size:13px;color:#222;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;">Dirección de envío</td>
                              <td style="padding:4px 0;font-size:13px;color:#222;">%s, %s, %s</td>
                            </tr>
                          </table>

                          <!-- Items table -->
                          <p style="margin:0 0 12px;font-size:14px;font-weight:700;color:#333;">Tus productos en esta orden</p>
                          <table width="100%%" cellpadding="0" cellspacing="0" style="border:1px solid #e8e8e8;border-radius:6px;overflow:hidden;">
                            <thead>
                              <tr style="background:#f0f0f0;">
                                <th style="padding:10px 8px;text-align:left;font-size:12px;color:#666;font-weight:600;">PRODUCTO</th>
                                <th style="padding:10px 8px;text-align:center;font-size:12px;color:#666;font-weight:600;">CANT.</th>
                                <th style="padding:10px 8px;text-align:right;font-size:12px;color:#666;font-weight:600;">PRECIO UNIT.</th>
                                <th style="padding:10px 8px;text-align:right;font-size:12px;color:#666;font-weight:600;">SUBTOTAL</th>
                              </tr>
                            </thead>
                            <tbody>
                              %s
                            </tbody>
                          </table>

                          <!-- Total -->
                          <table width="100%%" cellpadding="0" cellspacing="0" style="margin-top:12px;">
                            <tr>
                              <td style="text-align:right;padding:12px 8px;font-size:16px;color:#333;">
                                <strong>Total de tu parte:</strong>
                                <span style="color:#1a1a2e;font-size:18px;margin-left:12px;">$%s COP</span>
                              </td>
                            </tr>
                          </table>

                          <p style="margin:24px 0 0;font-size:13px;color:#888;">
                            Ingresa al panel de vendedor para gestionar esta orden y actualizar el estado del envío.
                          </p>

                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background:#f8f8f8;padding:20px 32px;text-align:center;">
                          <p style="margin:0;font-size:12px;color:#aaa;">© 2026 Forehapp · Todos los derechos reservados</p>
                        </td>
                      </tr>

                    </table>
                  </td></tr>
                </table>
                </body>
                </html>
                """.formatted(
                group.sellerName(),
                event.getOrderId(),
                event.getCreatedAt() != null ? event.getCreatedAt().format(DATE_FMT) : "-",
                event.getBuyerName(),
                event.getShippingAddress(), event.getShippingCity(), event.getShippingCountry(),
                rows.toString(),
                formatAmount(group.subtotal())
        );
    }

    private String formatAmount(BigDecimal amount) {
        return CURRENCY_FMT.format(amount);
    }
}
