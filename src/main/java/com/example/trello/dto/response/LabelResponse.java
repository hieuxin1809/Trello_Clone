package com.example.trello.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant; // Sửa sang Instant

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LabelResponse {
    String id;
    String boardId;
    String name;
    String color;
    Instant createdAt; // Sửa
    String createdBy;
}