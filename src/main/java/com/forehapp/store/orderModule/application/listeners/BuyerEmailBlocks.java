package com.forehapp.store.orderModule.application.listeners;

import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Blocks shared by the buyer emails (order confirmation and Mercado Pago payment confirmation). */
final class BuyerEmailBlocks {

    private BuyerEmailBlocks() {}

    /** Order registered by a seller for the buyer: say so, and give the data-processing notice. */
    static String assistedNotice(String registeredByStore) {
        if (registeredByStore == null) return "";
        return """
                <div style="margin-top:16px;background:#f3f4f8;border-radius:6px;padding:14px 16px;">
                  <p style="margin:0 0 6px;font-size:13px;color:#333;">
                    Este pedido lo registró <strong>%s</strong> en Forehapp Store a tu nombre.
                  </p>
                  <p style="margin:0;font-size:12px;color:#777;line-height:1.5;">
                    Usamos tus datos de contacto y envío solo para gestionar tus pedidos, de acuerdo con
                    la autorización que le diste al vendedor. Si no reconoces este pedido, responde a este
                    correo o contacta al vendedor.
                  </p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(registeredByStore));
    }

    /** Guest order: invite to create an account, where all orders made with this email will show up. */
    static String createAccountInvite(boolean guestBuyer, String frontendUrl, String buyerEmail) {
        if (!guestBuyer || buyerEmail == null) return "";
        String url = frontendUrl + "/register?email=" + URLEncoder.encode(buyerEmail, StandardCharsets.UTF_8);
        return """
                <div style="margin-top:16px;text-align:center;">
                  <p style="margin:0 0 10px;font-size:13px;color:#555;">
                    Crea tu cuenta con este correo para ver el estado de este y tus próximos pedidos.
                  </p>
                  <a href="%s" style="display:inline-block;background:#1a1a2e;color:#ffffff;text-decoration:none;font-size:13px;font-weight:600;padding:10px 20px;border-radius:6px;">Crear mi cuenta</a>
                </div>
                """.formatted(HtmlUtils.htmlEscape(url));
    }

    static String normalizeBaseUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
