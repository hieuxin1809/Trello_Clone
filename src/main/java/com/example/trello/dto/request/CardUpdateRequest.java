package com.example.trello.dto.request;

import com.example.trello.model.Card;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardUpdateRequest {
    String title;
    String description;
    Double ordering; // Dùng để di chuyển thẻ
    String listId; // Dùng để di chuyển thẻ giữa các list

    Instant dueDate;
    Instant dueReminder;
    Boolean isCompleted;
    Boolean isArchived;

    // Cập nhật nhãn và người được gán (chỉ là list ID/String đơn giản)
    List<Card.CardLabel> labels;
    // List<String> assigneeUserIds; // Chỉ lấy ID và Service xử lý logic gán
}