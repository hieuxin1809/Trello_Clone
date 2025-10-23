package com.example.trello.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notification_tokens")
public class NotificationToken {
    @Id
    private String id;
    private String userId;
    private String token;
}
