package com.example.trello.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "activities")
public class Activity {
    @Id
    private String id;

    @Indexed
    private String boardId;

    @Indexed
    private String cardId;

    private String listId;

    @Indexed
    private String userId;

    @Indexed
    private String action;

    private Map<String, Object> details;
    private Map<String, Object> metadata;

    @CreatedDate
    private Instant createdAt;
}
