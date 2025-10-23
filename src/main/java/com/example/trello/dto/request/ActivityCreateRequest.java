package com.example.trello.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityCreateRequest {
    String boardId;
    String cardId; // Optional
    String listId; // Optional
    String userId;
    String action;
    Map<String, Object> details;
    Map<String, Object> metadata;
}