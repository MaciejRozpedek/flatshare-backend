package com.flatshareteam.flatsharebackend.notifications.emailtemplate;

import com.flatshareteam.flatsharebackend.notifications.model.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;

@Component
public class PasswordResetEmailTemplate implements EmailTemplate {

    @Override
    public boolean supports(NotificationType type) {
        return NotificationType.PASSWORD_RESET == type;
    }

    @Override
    public String buildSubject(Map<String, Object> payload) {
        return "Reset hasła - FlatShare";
    }

    @Override
    public String buildHtmlBody(Map<String, Object> payload) {
        String resetToken = payload.get("resetToken") != null ? HtmlUtils.htmlEscape(payload.get("resetToken").toString()) : "";
        String content = "<h2>Reset hasła</h2>" +
                "<p>Otrzymaliśmy prośbę o zresetowanie hasła dla Twojego konta. Jeśli to nie Ty, zignoruj tę wiadomość.</p>" +
                "<p>Twój kod resetujący to:</p>" +
                "<div class=\"highlight-box\">" + resetToken + "</div>";
        return EmailTemplateWrapper.wrapInTemplate(content);
    }
}
