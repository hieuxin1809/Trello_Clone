package com.example.trello.dto.response;

import com.example.trello.model.Notification;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String id;
    String userId;
    String type;
    String title;
    String message;

    Notification.NotificationLink link;
    String triggeredBy;

    boolean isRead;
    Instant readAt;

    boolean emailSent;
    Instant emailSentAt;

    Instant createdAt;
}