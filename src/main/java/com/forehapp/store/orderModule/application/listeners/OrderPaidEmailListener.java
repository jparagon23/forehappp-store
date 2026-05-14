package com.forehapp.store.orderModule.application.listeners;

import com.forehapp.store.mail.EmailSender;
import com.forehapp.store.orderModule.domain.events.OrderPaidEvent;
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
public class OrderPaidEmailListener {

    private static final Logger log = LoggerFactory.getLogger(OrderPaidEmailListener.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getNumberInstance(new Locale("es", "CO"));

    private final EmailSender emailSender;

    public OrderPaidEmailListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        try {
            emailSender.sendEmail(
                    event.getBuyerEmail(),
                    "✅ Pago confirmado — Orden #" + event.getOrderId(),
                    buildEmail(event)
            );
            log.info("[OrderPaidEmail] Sent confirmation to buyer={} orderId={}", event.getBuyerEmail(), event.getOrderId());
        } catch (Exception e) {
            log.error("[OrderPaidEmail] Failed to send confirmation to buyer={} orderId={}", event.getBuyerEmail(), event.getOrderId(), e);
        }
    }

    private String buildEmail(OrderPaidEvent event) {
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
                          <p style="margin:4px 0 0;color:#a0a0c0;font-size:13px;">Confirmación de pago</p>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:32px;">

                          <!-- Checkmark -->
                          <div style="text-align:center;margin-bottom:24px;">
                            <div style="display:inline-block;width:64px;height:64px;background:#e8f5e9;border-radius:50%;line-height:64px;font-size:32px;">✓</div>
                          </div>

                          <h2 style="margin:0 0 8px;font-size:20px;color:#2e7d32;text-align:center;">¡Pago recibido!</h2>
                          <p style="margin:0 0 24px;font-size:14px;color:#555;text-align:center;">
                            Tu pago fue procesado exitosamente. Los vendedores ya están preparando tu pedido.
                          </p>

                          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f8f8f8;border-radius:6px;padding:16px;margin-bottom:24px;">
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;width:130px;">Número de orden</td>
                              <td style="padding:4px 0;font-size:13px;color:#222;font-weight:600;">#%d</td>
                            </tr>
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;">Comprador</td>
                              <td style="padding:4px 0;font-size:13px;color:#222;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;">Fecha</td>
                              <td style="padding:4px 0;font-size:13px;color:#222;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;">Total pagado</td>
                              <td style="padding:4px 0;font-size:15px;color:#1a1a2e;font-weight:700;">$%s COP</td>
                            </tr>
                          </table>

                          <p style="margin:0;font-size:13px;color:#888;text-align:center;">
                            Puedes revisar el estado de tu orden en tu perfil en cualquier momento.
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
                event.getOrderId(),
                event.getBuyerName(),
                event.getCreatedAt() != null ? event.getCreatedAt().format(DATE_FMT) : "-",
                formatAmount(event.getTotal())
        );
    }

    private String formatAmount(BigDecimal amount) {
        return CURRENCY_FMT.format(amount);
    }
}
