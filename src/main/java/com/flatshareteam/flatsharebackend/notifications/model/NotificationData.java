package com.flatshareteam.flatsharebackend.notifications.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NotificationData {
    private NotificationType type;
    private Map<String, Object> payload;

    public NotificationData() {
    }

    public NotificationData(NotificationType type, Map<String, Object> payload) {
        this.type = type;
        this.payload = payload;
    }
}
