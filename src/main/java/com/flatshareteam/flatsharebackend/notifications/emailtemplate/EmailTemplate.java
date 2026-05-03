package com.flatshareteam.flatsharebackend.notifications.emailtemplate;

import com.flatshareteam.flatsharebackend.notifications.model.NotificationType;
import java.util.Map;

public interface EmailTemplate {
    boolean supports(NotificationType type);
    String buildSubject(Map<String, Object> payload);
    String buildHtmlBody(Map<String, Object> payload);
}
