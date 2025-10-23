package com.example.trello.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoardUpdateRequest {
    String title;
    String description;
    String visibility;
    Boolean isClosed;
    Boolean isArchived;
    // Có thể thêm cập nhật background
}