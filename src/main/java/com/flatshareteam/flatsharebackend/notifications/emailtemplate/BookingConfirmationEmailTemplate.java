package com.flatshareteam.flatsharebackend.notifications.emailtemplate;

import com.flatshareteam.flatsharebackend.notifications.model.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;

@Component
public class BookingConfirmationEmailTemplate implements EmailTemplate {

    @Override
    public boolean supports(NotificationType type) {
        return NotificationType.BOOKING_CONFIRMATION == type;
    }

    @Override
    public String buildSubject(Map<String, Object> payload) {
        return "Potwierdzenie rezerwacji - FlatShare";
    }

    @Override
    public String buildHtmlBody(Map<String, Object> payload) {
        String bookingId = payload.get("bookingId") != null ? HtmlUtils.htmlEscape(payload.get("bookingId").toString()) : "";
        String content = "<h2>Potwierdzenie rezerwacji 🎉</h2>" +
                "<p>Mamy świetne wieści! Twoja rezerwacja została pomyślnie potwierdzona.</p>" +
                "<div class=\"highlight-box\">ID Rezerwacji: " + bookingId + "</div>" +
                "<p>Wejdź w szczegóły swojej rezerwacji w aplikacji, aby zobaczyć więcej informacji.</p>";
        return EmailTemplateWrapper.wrapInTemplate(content);
    }
}
