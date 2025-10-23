package com.example.trello.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardMoveRequest {
    String newListId;    // ID của List đích
    Integer newPosition; // Vị trí mới trong List đích
    String movedBy;      // ID của người thực hiện thao tác
}