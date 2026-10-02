package com.forehapp.store.repurchaseModule.application.usecases;

import com.forehapp.store.repurchaseModule.domain.model.ReminderPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

@Component
public class RepurchaseEmailBuilder {

    private final UnsubscribeTokenService tokenService;
    private final String frontendUrl;
    private final String productPath;
    private final String unsubscribePath;

    public RepurchaseEmailBuilder(UnsubscribeTokenService tokenService,
                                  @Value("${app.frontend.url}") String frontendUrl,
                                  @Value("${app.repurchase.product-path}") String productPath,
                                  @Value("${app.repurchase.unsubscribe-path}") String unsubscribePath) {
        this.tokenService = tokenService;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        this.productPath = productPath;
        this.unsubscribePath = unsubscribePath;
    }

    public String buildSubject(ReminderPlan plan) {
        if (plan.items().size() == 1) {
            return "¿Se te está acabando? Repón tu " + plan.items().get(0).productTitle();
        }
        return "¿Se te están acabando? Es hora de reponer tus productos";
    }

    public String buildHtml(ReminderPlan plan, Map<Long, String> thumbnailUrls, LocalDate today) {
        StringBuilder items = new StringBuilder();
        for (ReminderPlan.Item item : plan.items()) {
            items.append(buildItem(item, thumbnailUrls.get(item.productId()), today));
        }

        String greeting = plan.name().isEmpty() ? "¡Hola!" : "¡Hola, " + HtmlUtils.htmlEscape(plan.name()) + "!";

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
                          <p style="margin:4px 0 0;color:#a0a0c0;font-size:13px;">Es hora de reponer</p>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:32px;">
                          <h2 style="margin:0 0 8px;font-size:20px;color:#1a1a2e;">%s</h2>
                          <p style="margin:0 0 24px;font-size:14px;color:#555;line-height:1.5;">
                            Por el tiempo que ha pasado desde tu compra, calculamos que pronto vas a necesitar más.
                            Te dejamos el acceso directo para que no te quedes sin jugar.
                          </p>

                          %s
                        </td>
                      </tr>

                      <tr>
                        <td style="background:#f8f8f8;padding:18px 32px;text-align:center;">
                          <p style="margin:0 0 6px;font-size:12px;color:#aaa;">
                            Recibes este correo porque compraste en Forehapp Store.
                            <a href="%s" style="color:#888;">Dejar de recibir estos recordatorios</a>
                          </p>
                          <p style="margin:0;font-size:12px;color:#aaa;">© %d Forehapp · Todos los derechos reservados</p>
                        </td>
                      </tr>

                    </table>
                  </td></tr>
                </table>
                </body>
                </html>
                """.formatted(greeting, items, unsubscribeUrl(plan.email()), today.getYear());
    }

    private String buildItem(ReminderPlan.Item item, String thumbnailUrl, LocalDate today) {
        String image = thumbnailUrl == null ? "" : """
                <td width="88" valign="top" style="padding-right:16px;">
                  <img src="%s" width="88" height="88" alt="" style="display:block;border-radius:6px;object-fit:cover;">
                </td>
                """.formatted(HtmlUtils.htmlEscape(thumbnailUrl));

        return """
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f8f8f8;border-radius:6px;padding:16px;margin-bottom:16px;">
                  <tr>
                    %s
                    <td valign="top">
                      <p style="margin:0 0 4px;font-size:15px;color:#222;font-weight:600;">%s</p>
                      <p style="margin:0 0 4px;font-size:13px;color:#888;">Lo compraste %s</p>
                      <p style="margin:0 0 12px;font-size:15px;color:#1a1a2e;font-weight:700;">$%s COP</p>
                      <a href="%s" style="display:inline-block;background:#1a1a2e;color:#ffffff;text-decoration:none;font-size:13px;font-weight:600;padding:10px 18px;border-radius:6px;">Comprar de nuevo</a>
                    </td>
                  </tr>
                </table>
                """.formatted(
                image,
                HtmlUtils.htmlEscape(item.productTitle()),
                timeAgo(item.purchasedOn(), today),
                formatAmount(item.currentPrice()),
                productUrl(item.productId()));
    }

    static String timeAgo(LocalDate purchasedOn, LocalDate today) {
        long days = Math.max(1, ChronoUnit.DAYS.between(purchasedOn, today));
        if (days < 14) return "hace " + days + (days == 1 ? " día" : " días");
        if (days < 60) return "hace " + (days / 7) + " semanas";
        return "hace " + (days / 30) + " meses";
    }

    private String productUrl(Long productId) {
        return frontendUrl + productPath.replace("{productId}", String.valueOf(productId))
                + "?utm_source=repurchase_reminder&utm_medium=email";
    }

    private String unsubscribeUrl(String email) {
        return frontendUrl + unsubscribePath + "?token="
                + URLEncoder.encode(tokenService.createToken(email), StandardCharsets.UTF_8);
    }

    private String formatAmount(BigDecimal amount) {
        return NumberFormat.getNumberInstance(new Locale("es", "CO")).format(amount);
    }
}
