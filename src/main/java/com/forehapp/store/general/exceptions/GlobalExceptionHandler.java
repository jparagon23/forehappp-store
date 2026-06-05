package com.forehapp.store.general.exceptions;

import com.forehapp.store.general.dto.ErrorResponse;
import com.forehapp.store.mail.EmailSender;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter ALERT_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_STACK_CHARS = 3000;

    private final EmailSender emailSender;
    private final List<String> adminEmails;

    public GlobalExceptionHandler(EmailSender emailSender,
                                   @Value("${app.alert.admin-emails:}") String adminEmailsCsv) {
        this.emailSender = emailSender;
        this.adminEmails = Arrays.stream(adminEmailsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ErrorCode.VALIDATION_ERROR, message));
    }

    // BUG-03/BUG-C: concurrent duplicate add hits unique constraint (cart or wishlist)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ErrorCode.DATA_INTEGRITY_VIOLATION,
                        "El ítem ya existe. Recarga e intenta de nuevo."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR, ex.getReason()));
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ErrorResponse> handleNotWritable(HttpMessageNotWritableException ex,
                                                            HttpServletRequest request) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof IOException && cause.getMessage() != null && cause.getMessage().contains("Broken pipe")) {
            log.debug("Client disconnected before response was fully written");
            return null;
        }
        log.error("Could not write HTTP response", ex);
        sendAlertEmails(ex, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR, "Internal server error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        sendAlertEmails(ex, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR, "Internal server error"));
    }

    private void sendAlertEmails(Exception ex, HttpServletRequest request) {
        if (adminEmails.isEmpty()) return;
        try {
            String timestamp = LocalDateTime.now().format(ALERT_DATE_FMT);
            String subject = "[500] " + ex.getClass().getSimpleName() + " — " + timestamp;
            String html = buildAlertEmail(ex, request, timestamp);
            for (String email : adminEmails) {
                emailSender.sendEmail(email, subject, html).exceptionally(t -> {
                    log.warn("[AlertEmail] Failed to send 500 alert to {}: {}", email, t.getMessage());
                    return null;
                });
            }
        } catch (Exception e) {
            log.warn("[AlertEmail] Could not build/send alert email: {}", e.getMessage());
        }
    }

    private String buildAlertEmail(Exception ex, HttpServletRequest request, String timestamp) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stack = sw.toString();
        if (stack.length() > MAX_STACK_CHARS) {
            stack = stack.substring(0, MAX_STACK_CHARS) + "\n... (truncated)";
        }
        stack = stack.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        String method = request != null ? request.getMethod() : "N/A";
        String uri    = request != null ? request.getRequestURI() : "N/A";

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:32px 0;">
                  <tr><td align="center">
                    <table width="640" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                      <tr>
                        <td style="background:#b71c1c;padding:24px 32px;">
                          <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:700;">⚠ Error 500 — Forehapp Store</h1>
                          <p style="margin:6px 0 0;color:#ffcdd2;font-size:13px;">Se produjo una excepción no controlada en producción</p>
                        </td>
                      </tr>

                      <tr>
                        <td style="padding:28px 32px;">

                          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#fafafa;border:1px solid #e0e0e0;border-radius:6px;padding:16px;margin-bottom:24px;">
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;width:160px;">Timestamp</td>
                              <td style="padding:4px 0;font-size:13px;color:#222;font-weight:600;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;">Request</td>
                              <td style="padding:4px 0;font-size:13px;color:#222;font-weight:600;">%s %s</td>
                            </tr>
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;">Excepción</td>
                              <td style="padding:4px 0;font-size:13px;color:#b71c1c;font-weight:600;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:4px 0;font-size:13px;color:#888;">Mensaje</td>
                              <td style="padding:4px 0;font-size:13px;color:#333;">%s</td>
                            </tr>
                          </table>

                          <p style="margin:0 0 10px;font-size:13px;font-weight:700;color:#333;">Stack trace</p>
                          <pre style="margin:0;background:#212121;color:#f5f5f5;padding:16px;border-radius:6px;font-size:11px;overflow-x:auto;white-space:pre-wrap;word-break:break-all;">%s</pre>

                        </td>
                      </tr>

                      <tr>
                        <td style="background:#f8f8f8;padding:16px 32px;text-align:center;">
                          <p style="margin:0;font-size:12px;color:#aaa;">© 2026 Forehapp · Alerta automática de sistema</p>
                        </td>
                      </tr>

                    </table>
                  </td></tr>
                </table>
                </body>
                </html>
                """.formatted(
                timestamp,
                method, uri,
                ex.getClass().getName(),
                ex.getMessage() != null ? ex.getMessage().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") : "(sin mensaje)",
                stack
        );
    }
}
