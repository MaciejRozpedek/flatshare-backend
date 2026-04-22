package com.flatshareteam.flatsharebackend.notifications.port;

import com.flatshareteam.flatsharebackend.notifications.model.NotificationData;

import java.util.UUID;

public interface INotificationPort {
    void notify(UUID userId, NotificationData message);
}
