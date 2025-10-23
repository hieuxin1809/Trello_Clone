package com.example.trello.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListResponse {
    String id;
    String boardId;
    String title;
    int position;
    boolean isArchived;
    Instant createdAt; // Sửa
    Instant updatedAt; // Sửa
    String createdBy;
}
