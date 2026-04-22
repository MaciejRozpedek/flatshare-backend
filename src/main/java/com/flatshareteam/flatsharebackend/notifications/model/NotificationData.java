package com.flatshareteam.flatsharebackend.notifications.model;

import java.util.Map;

public class NotificationData {
    private NotificationType type;
    private Map<String, Object> payload;

    public NotificationData() {
    }

    public NotificationData(NotificationType type, Map<String, Object> payload) {
        this.type = type;
        this.payload = payload;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
