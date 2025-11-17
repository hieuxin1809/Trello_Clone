package com.example.trello.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentCreateRequest {
    @NotBlank(message = "CARD_ID_REQUIRED")
    String cardId;
    @NotBlank(message = "BOARD_ID_REQUIRED")
    String boardId;
    @NotBlank(message = "USER_ID_REQUIRED")
    String userId; // Người comment
    @NotBlank(message = "CONTENT_REQUIRED")
    @Size(max = 1000, message = "COMMENT_TOO_LONG")
    String content;
    List<String> mentions; // List user IDs được mention
}
