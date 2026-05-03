package com.flatshareteam.flatsharebackend.notifications.emailtemplate;

import com.flatshareteam.flatsharebackend.notifications.model.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;

@Component
public class ListingBlockedEmailTemplate implements EmailTemplate {

    @Override
    public boolean supports(NotificationType type) {
        return NotificationType.LISTING_BLOCKED == type;
    }

    @Override
    public String buildSubject(Map<String, Object> payload) {
        return "Ważna informacja: Twoje ogłoszenie zostało zablokowane";
    }

    @Override
    public String buildHtmlBody(Map<String, Object> payload) {
        String reason = payload.get("reason") != null ? HtmlUtils.htmlEscape(payload.get("reason").toString()) : "";
        String content = "<h2>Ważna informacja o ogłoszeniu ⚠️</h2>" +
                "<p>Niestety, jedno z Twoich ogłoszeń zostało zablokowane przez zespół moderacji.</p>" +
                "<div class=\"highlight-box\" style=\"border-left-color: #ef4444;\">Powód: " + reason + "</div>" +
                "<p>Skontaktuj się z naszym wsparciem, jeśli uważasz, że to pomyłka, i postaramy się wyjaśnić sprawę.</p>";
        return EmailTemplateWrapper.wrapInTemplate(content);
    }
}
