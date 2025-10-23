package com.example.trello.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListUpdateRequest {
    String title;
    Boolean isArchived;
    Integer position; // Dùng để thay đổi vị trí (di chuyển list)
}
