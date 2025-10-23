package com.example.trello.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id;
    String cardId;
    String boardId;
    String userId;
    String content;
    List<String> mentions;

    boolean isEdited;
    Instant editedAt;
    Instant createdAt;
    Instant updatedAt;
}