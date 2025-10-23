package com.example.trello.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoardCreateRequest {
    String title;
    String description;
    String ownerId; // ID của người tạo
    String visibility; // PRIVATE, TEAM, PUBLIC
}