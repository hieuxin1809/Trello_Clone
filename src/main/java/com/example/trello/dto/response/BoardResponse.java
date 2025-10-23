package com.example.trello.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoardResponse {
    String id;
    String title;
    String description;
    String ownerId;
    String visibility;
    boolean isClosed;
    boolean isArchived;
    Instant createdAt;
    Instant updatedAt;
}