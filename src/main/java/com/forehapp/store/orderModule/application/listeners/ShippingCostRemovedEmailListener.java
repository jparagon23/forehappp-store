package com.forehapp.store.orderModule.application.listeners;

import com.forehapp.store.mail.EmailSender;
import com.forehapp.store.orderModule.domain.events.ShippingCostRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ShippingCostRemovedEmailListener {

    private static final Logger log = LoggerFactory.getLogger(ShippingCostRemovedEmailListener.class);

    private final EmailSender emailSender;

    public ShippingCostRemovedEmailListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShippingCostRemoved(ShippingCostRemovedEvent event) {
        try {
            emailSender.sendEmail(event.getBuyerEmail(),
                    "🚚 Te quitamos el costo de envío — Orden #" + event.getOrderId(),
                    buildEmail(event));
            log.info("[ShippingCostRemovedEmail] buyer={} orderId={} groupId={} waived={}",
                    event.getBuyerEmail(), event.getOrderId(), event.getGroupId(), event.getWaivedAmount());
        } catch (Exception e) {
            log.error("[ShippingCostRemovedEmail] Failed buyer={} orderId={} groupId={}",
                    event.getBuyerEmail(), event.getOrderId(), event.getGroupId(), e);
        }
    }

    private String buildEmail(ShippingCostRemovedEvent event) {
        String body = """
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#e8f5e9;border-left:4px solid #2e7d32;border-radius:4px;padding:16px 20px;margin-bottom:24px;">
                  <tr>
                    <td style="font-size:13px;color:#555;width:150px;">Orden #</td>
                    <td style="font-size:16px;color:#1b5e20;font-weight:700;">#%d</td>
                  </tr>
                  <tr>
                    <td style="padding-top:6px;font-size:13px;color:#555;">Costo de envío eliminado</td>
                    <td style="padding-top:6px;font-size:16px;color:#1b5e20;font-weight:700;">$%,.2f</td>
                  </tr>
                  <tr>
                    <td style="padding-top:6px;font-size:13px;color:#555;">Nuevo valor del pedido</td>
                    <td style="padding-top:6px;font-size:16px;color:#1b5e20;font-weight:700;">$%,.2f</td>
                  </tr>
                  <tr>
                    <td style="padding-top:8px;font-size:13px;color:#555;vertical-align:top;">Motivo</td>
                    <td style="padding-top:8px;font-size:13px;color:#222;">%s</td>
                  </tr>
                </table>
                """.formatted(event.getOrderId(), event.getWaivedAmount(), event.getNewOrderTotal(), event.getReason());

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
                            <div style="display:inline-block;width:64px;height:64px;background:#e8f5e9;border-radius:50%%;line-height:64px;font-size:32px;">🚚</div>
                          </div>
                          <h2 style="margin:0 0 8px;font-size:20px;color:#2e7d32;text-align:center;">¡Te quitamos el costo de envío!</h2>
                          <p style="margin:0 0 24px;font-size:14px;color:#555;text-align:center;">Hola <strong>%s</strong>, <strong>%s</strong> decidió eliminar el costo de envío de tu pedido.</p>
                          %s
                          <p style="margin:0;font-size:13px;color:#888;text-align:center;">Si tienes dudas, por favor contáctanos a través de la plataforma.</p>
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
                """.formatted(event.getBuyerName(), event.getStoreName(), body);
    }
}
