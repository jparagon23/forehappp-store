package com.forehapp.store.supplierSyncModule.application.usecases;

import com.forehapp.store.supplierSyncModule.application.usecases.SupplierSyncWriter.RunOutcome;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncEvent;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncRun;
import com.forehapp.store.supplierSyncModule.domain.model.SyncEventType;
import com.forehapp.store.supplierSyncModule.domain.model.SyncRunStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** Daily summary email of a supplier sync, one section per store. */
@Component
public class SupplierSyncEmailBuilder {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MAX_ROWS = 60;

    public String buildSubject(List<RunOutcome> outcomes, LocalDate today) {
        int alerts = outcomes.stream().mapToInt(o -> o.run().getMarginAlerts()).sum();
        boolean aborted = outcomes.stream().anyMatch(o -> o.run().getStatus() == SyncRunStatus.ABORTED);
        String prefix = aborted ? "⚠ Sincronización detenida" : "Sincronización Profitness";
        return prefix + " — " + today.format(DATE_FMT) + (alerts > 0 ? " — " + alerts + " alerta(s) de margen" : "");
    }

    public String buildHtml(List<RunOutcome> outcomes, Map<Long, String> storeNames) {
        StringBuilder body = new StringBuilder();
        for (RunOutcome outcome : outcomes) {
            body.append(storeSection(outcome, storeNames.getOrDefault(outcome.run().getStoreId(),
                    "Tienda #" + outcome.run().getStoreId())));
        }
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:24px 0;">
                  <tr><td align="center">
                    <table width="720" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;">
                      <tr>
                        <td style="background:#1a1a2e;padding:20px 28px;">
                          <h1 style="margin:0;color:#ffffff;font-size:18px;">Forehapp Store</h1>
                          <p style="margin:4px 0 0;color:#a0a0c0;font-size:13px;">Sincronización diaria con el proveedor</p>
                        </td>
                      </tr>
                      <tr><td style="padding:24px 28px;">%s</td></tr>
                    </table>
                  </td></tr>
                </table>
                </body>
                </html>
                """.formatted(body);
    }

    private String storeSection(RunOutcome outcome, String storeName) {
        SupplierSyncRun run = outcome.run();
        StringBuilder html = new StringBuilder();
        html.append("<h2 style=\"margin:0 0 6px;font-size:17px;color:#1a1a2e;\">")
                .append(esc(storeName)).append("</h2>");
        html.append(statusBanner(run));
        html.append(summaryTable(run, outcome.pendingSuggestions()));

        List<SupplierSyncEvent> events = outcome.events();
        boolean preview = run.getStatus() != SyncRunStatus.APPLIED;
        html.append(section(events, SyncEventType.MARGIN_ALERT, "Margen menor al mínimo",
                "Precio venta / precio proveedor", "Margen", "#c62828"));
        html.append(section(events, SyncEventType.DISABLED,
                preview ? "Se apagarían (agotados en el proveedor)" : "Apagados (agotados en el proveedor)",
                "Stock anterior", "Stock", "#c62828"));
        html.append(section(events, SyncEventType.REENABLED,
                preview ? "Se reactivarían" : "Reactivados", "Stock anterior", "Stock", "#2e7d32"));
        html.append(section(events, SyncEventType.ORDER_AT_RISK, "Pedidos abiertos con productos agotados",
                "Pedido", "Cantidad", "#e65100"));
        html.append(section(events, SyncEventType.BROKEN_LINK, "Parejas rotas (no aparecen en el catálogo del proveedor)",
                null, null, "#e65100"));
        html.append(section(events, SyncEventType.COST_UPDATED,
                preview ? "Costos que se actualizarían" : "Costos actualizados", "Costo anterior", "Costo nuevo", "#1565c0"));
        html.append(section(events, SyncEventType.RELEASED, "Repuestos a mano por el seller", null, "Stock", "#555555"));
        html.append("<hr style=\"border:none;border-top:1px solid #eee;margin:24px 0;\">");
        return html.toString();
    }

    private String statusBanner(SupplierSyncRun run) {
        String text;
        String color;
        if (run.getStatus() == SyncRunStatus.ABORTED) {
            text = "Detenida por seguridad: no se aplicó ningún cambio. " + esc(run.getAbortReason());
            color = "#c62828";
        } else if (run.getStatus() == SyncRunStatus.PREVIEW) {
            text = "Modo vista previa: esto es lo que haría la sincronización. No se cambió stock ni costos.";
            color = "#e65100";
        } else {
            text = "Cambios aplicados.";
            color = "#2e7d32";
        }
        return "<p style=\"margin:0 0 12px;padding:10px 12px;border-radius:6px;font-size:13px;color:#fff;background:"
                + color + ";\">" + text + "</p>";
    }

    private String summaryTable(SupplierSyncRun run, int pendingSuggestions) {
        String[][] rows = {
                {"Productos en el proveedor", String.valueOf(run.getSupplierItems())},
                {"Agotados en el proveedor", String.valueOf(run.getSupplierOutOfStock())},
                {"Variantes con pareja confirmada", String.valueOf(run.getConfirmedLinks())},
                {"Sugerencias pendientes por revisar", String.valueOf(pendingSuggestions)},
        };
        StringBuilder html = new StringBuilder("<table cellpadding=\"0\" cellspacing=\"0\" style=\"font-size:13px;margin-bottom:12px;\">");
        for (String[] row : rows) {
            html.append("<tr><td style=\"padding:2px 16px 2px 0;color:#888;\">").append(row[0])
                    .append("</td><td style=\"padding:2px 0;color:#222;font-weight:600;\">").append(row[1]).append("</td></tr>");
        }
        return html.append("</table>").toString();
    }

    private String section(List<SupplierSyncEvent> events, SyncEventType type, String title,
                           String oldHeader, String newHeader, String color) {
        List<SupplierSyncEvent> rows = events.stream().filter(e -> e.getType() == type).toList();
        if (rows.isEmpty()) return "";

        StringBuilder html = new StringBuilder();
        html.append("<h3 style=\"margin:20px 0 8px;font-size:14px;color:").append(color).append(";\">")
                .append(title).append(" (").append(rows.size()).append(")</h3>");
        html.append("<table width=\"100%\" cellpadding=\"6\" cellspacing=\"0\" style=\"font-size:12px;border-collapse:collapse;\">");
        html.append("<tr style=\"background:#f2f3f4;color:#555;text-align:left;\"><th>Producto</th><th>Variante</th><th>Proveedor</th>");
        if (oldHeader != null) html.append("<th>").append(oldHeader).append("</th>");
        if (newHeader != null) html.append("<th>").append(newHeader).append("</th>");
        html.append("</tr>");

        for (SupplierSyncEvent e : rows.subList(0, Math.min(rows.size(), MAX_ROWS))) {
            html.append("<tr style=\"border-bottom:1px solid #eee;\">")
                    .append(cell(e.getProductTitle()))
                    .append(cell(e.getVariantLabel()))
                    .append(cell(e.getSupplierItemName() == null ? null : e.getSupplierItemName()
                            + (Boolean.TRUE.equals(e.getUnconfirmed()) ? " (pareja sin confirmar)" : "")));
            if (oldHeader != null) html.append(cell(e.getOldValue()));
            if (newHeader != null) html.append(cell(e.getNewValue()));
            html.append("</tr>");
        }
        html.append("</table>");
        if (rows.size() > MAX_ROWS) {
            html.append("<p style=\"font-size:12px;color:#888;\">… y ").append(rows.size() - MAX_ROWS)
                    .append(" más. Revisa el detalle en el panel del seller.</p>");
        }
        return html.toString();
    }

    private static String cell(String value) {
        return "<td style=\"color:#222;\">" + esc(value) + "</td>";
    }

    private static String esc(String value) {
        return value == null ? "—" : HtmlUtils.htmlEscape(value);
    }
}
