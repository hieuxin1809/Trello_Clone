package com.example.trello.dto.response;

import com.example.trello.model.Card;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardResponse {
    String id;
    String listId;
    String boardId;
    String title;
    String description;
    Double ordering;

    List<Card.CardLabel> labels;
    List<Card.CardAssignee> assignees;

    Instant dueDate;
    boolean isCompleted;
    Instant completedAt;

    boolean isArchived;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
}