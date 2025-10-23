package com.example.trello.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardAssigneeRequest {
    String userId;      // ID của User được gán/hủy gán
    String assignedBy;  // ID của người thực hiện thao tác (quản lý)
}