package com.forehapp.store.orderModule.application.listeners;

import com.forehapp.store.mail.EmailSender;
import com.forehapp.store.orderModule.domain.events.OrderItemsChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;

/** Tells the buyer which products of their order the store changed, the new total and what to pay. */
@Component
public class OrderItemsChangedEmailListener {

    private static final Logger log = LoggerFactory.getLogger(OrderItemsChangedEmailListener.class);

    private final EmailSender emailSender;

    public OrderItemsChangedEmailListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onItemsChanged(OrderItemsChangedEvent event) {
        if (event.buyerEmail() == null || event.buyerEmail().isBlank()) return;
        try {
            emailSender.sendEmail(event.buyerEmail(),
                    "📝 Actualizamos los productos de tu pedido #" + event.orderId(),
                    buildEmail(event));
            log.info("[OrderItemsChangedEmail] buyer={} orderId={} lines={}",
                    event.buyerEmail(), event.orderId(), event.lines().size());
        } catch (Exception e) {
            log.error("[OrderItemsChangedEmail] Failed buyer={} orderId={}", event.buyerEmail(), event.orderId(), e);
        }
    }

    public String buildEmail(OrderItemsChangedEvent event) {
        StringBuilder rows = new StringBuilder();
        for (OrderItemsChangedEvent.Line line : event.lines()) {
            rows.append("<tr style=\"border-bottom:1px solid #eee;\">")
                    .append(cell(typeLabel(line.type()), "font-weight:700;color:" + typeColor(line.type()) + ";white-space:nowrap;"))
                    .append(cell(line.oldLabel() == null ? "—" : esc(line.oldLabel()) + detail(line.oldQuantity(), line.oldUnitPrice()), "color:#888;"))
                    .append(cell(line.newLabel() == null ? "—" : esc(line.newLabel()) + detail(line.newQuantity(), line.newUnitPrice()), "color:#222;"))
                    .append("</tr>");
        }

        String body = "<p style=\"margin:0 0 6px;font-size:13px;color:#555;\"><strong>Motivo:</strong> " + esc(event.reason()) + "</p>"
                + "<table width=\"100%\" cellpadding=\"8\" cellspacing=\"0\" style=\"font-size:13px;border-collapse:collapse;margin:14px 0 20px;\">"
                + "<tr style=\"background:#f2f3f4;color:#555;text-align:left;\"><th>Cambio</th><th>Antes</th><th>Ahora</th></tr>"
                + rows
                + "</table>"
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f8f9fb;border-radius:6px;padding:14px 18px;margin-bottom:20px;font-size:14px;\">"
                + "<tr><td style=\"color:#555;\">Total anterior</td><td style=\"text-align:right;color:#888;\">" + money(event.orderTotalBefore()) + "</td></tr>"
                + "<tr><td style=\"padding-top:6px;color:#222;font-weight:700;\">Nuevo total</td><td style=\"padding-top:6px;text-align:right;color:#1a1a2e;font-weight:700;font-size:16px;\">" + money(event.orderTotalAfter()) + "</td></tr>"
                + "</table>"
                + paymentBlock(event);

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:32px 0;">
                  <tr><td align="center">
                    <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                      <tr>
                        <td style="background:#1a1a2e;padding:24px 32px;">
                          <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:700;">Forehapp Store</h1>
                          <p style="margin:4px 0 0;color:#a0a0c0;font-size:13px;">Actualización de tu pedido</p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:32px;">
                          <h2 style="margin:0 0 8px;font-size:20px;color:#1a1a2e;">Actualizamos tu pedido #%d</h2>
                          <p style="margin:0 0 18px;font-size:14px;color:#555;">Hola <strong>%s</strong>, <strong>%s</strong> hizo estos cambios en los productos de tu pedido, como lo acordaron.</p>
                          %s
                          <p style="margin:18px 0 0;font-size:13px;color:#888;">Si no estás de acuerdo con el cambio o tienes dudas, responde a este correo o contacta a la tienda.</p>
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
                """.formatted(event.orderId(), esc(event.buyerName()), esc(event.storeName()), body);
    }

    private static String paymentBlock(OrderItemsChangedEvent event) {
        if (event.paymentUrl() != null) {
            return box("#e3f2fd", "#1565c0",
                    "Tu pedido aún no está pagado. Usa este <strong>nuevo link</strong> para pagar el nuevo total; "
                            + "el link anterior tenía el valor viejo.")
                    + "<p style=\"text-align:center;margin:16px 0 0;\"><a href=\"" + esc(event.paymentUrl())
                    + "\" style=\"display:inline-block;background:#009ee3;color:#ffffff;text-decoration:none;font-weight:700;"
                    + "padding:12px 28px;border-radius:6px;\">Pagar " + money(event.orderTotalAfter()) + "</a></p>";
        }
        if (event.paymentPending()) {
            return box("#f2f3f4", "#333", "Tu pedido aún no está pagado: el valor a pagar ahora es <strong>"
                    + money(event.orderTotalAfter()) + "</strong>.");
        }
        BigDecimal balance = event.balanceDue();
        if (balance == null || balance.signum() == 0) {
            return box("#e8f5e9", "#1b5e20", "Tu pago ya cubre el pedido: no tienes que hacer nada más.");
        }
        if (balance.signum() > 0) {
            return box("#fff3e0", "#e65100", "Como ya pagaste, queda una <strong>diferencia por pagar de "
                    + money(balance) + "</strong>. La tienda se pondrá en contacto contigo para coordinarla.");
        }
        return box("#e8f5e9", "#1b5e20", "Como ya pagaste, tienes un <strong>saldo a favor de "
                + money(balance.negate()) + "</strong>. La tienda se pondrá en contacto contigo para devolvértelo.");
    }

    private static String box(String background, String color, String html) {
        return "<div style=\"background:" + background + ";color:" + color + ";border-radius:6px;padding:14px 18px;font-size:14px;\">"
                + html + "</div>";
    }

    private static String typeLabel(String type) {
        return switch (type) {
            case "REPLACED" -> "Reemplazado";
            case "UPDATED" -> "Modificado";
            case "ADDED" -> "Agregado";
            case "REMOVED" -> "Retirado";
            default -> type;
        };
    }

    private static String typeColor(String type) {
        return switch (type) {
            case "ADDED" -> "#2e7d32";
            case "REMOVED" -> "#c62828";
            default -> "#1565c0";
        };
    }

    private static String detail(Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || unitPrice == null) return "";
        return "<br><span style=\"font-size:12px;color:#888;\">" + quantity + " × " + money(unitPrice) + "</span>";
    }

    private static String cell(String html, String style) {
        return "<td style=\"vertical-align:top;" + style + "\">" + html + "</td>";
    }

    private static String money(BigDecimal value) {
        return String.format("$%,.2f", value == null ? BigDecimal.ZERO : value);
    }

    private static String esc(String value) {
        return value == null ? "" : HtmlUtils.htmlEscape(value);
    }
}
