package com.flatshareteam.flatsharebackend.notifications.emailtemplate;

import com.flatshareteam.flatsharebackend.notifications.model.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RegistrationConfirmationEmailTemplate implements EmailTemplate {

    @Override
    public boolean supports(NotificationType type) {
        return NotificationType.REGISTRATION_CONFIRMATION == type;
    }

    @Override
    public String buildSubject(Map<String, Object> payload) {
        return "Witamy we FlatShare!";
    }

    @Override
    public String buildHtmlBody(Map<String, Object> payload) {
        String content = "<h2>Witaj na pokładzie, " + payload.get("firstName") + "! 👋</h2>" +
                "<p>Cieszymy się, że dołączyłeś do społeczności <b>FlatShare</b>.</p>" +
                "<p>Nasza platforma pomoże Ci szybko i bezpiecznie znaleźć idealny pokój lub współlokatora.</p>" +
                "<div style=\"text-align: left;\"><a href=\"#\" class=\"btn\">Przejdź do aplikacji</a></div>";
        return EmailTemplateWrapper.wrapInTemplate(content);
    }
}
