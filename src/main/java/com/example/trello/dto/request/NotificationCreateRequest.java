package com.example.trello.dto.request;

import com.example.trello.model.Notification;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationCreateRequest {
    String userId; // Người nhận
    String type;
    String title;
    String message;

    Notification.NotificationLink link;
    String triggeredBy; // Người gây ra hành động
}
