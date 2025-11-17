package com.example.trello.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardCreateRequest {
    @NotBlank(message = "LIST_ID_REQUIRED")
    String listId;
    @NotBlank(message = "BOARD_ID_REQUIRED")
    String boardId;
    @NotBlank(message = "TITLE_REQUIRED")
    @Size(max = 100, message = "TITLE_TOO_LONG")
    String title;
    String description;
    @NotBlank(message = "CREATOR_ID_REQUIRED")
    String createdBy;

    @Future(message = "DUE_DATE_MUST_BE_IN_FUTURE")
    Instant dueDate;
}