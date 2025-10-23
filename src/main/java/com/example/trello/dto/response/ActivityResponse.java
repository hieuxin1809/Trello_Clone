package com.example.trello.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityResponse {
    String id;
    String boardId;
    String cardId;
    String listId;
    String userId;
    String action;
    Map<String, Object> details;
    Map<String, Object> metadata;
    Instant createdAt;
}
