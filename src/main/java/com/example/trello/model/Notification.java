package com.example.trello.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String type;
    private String title;
    private String message;

    private NotificationLink link;
    private String triggeredBy;

    @Indexed
    private boolean isRead = false;
    private Instant readAt;

    private boolean emailSent = false;
    private Instant emailSentAt;

    @CreatedDate
    private Instant createdAt;

    @Data
    public static class NotificationLink {
        private String type;
        private String boardId;
        private String cardId;
    }
}
